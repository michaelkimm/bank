package com.ms.bank.transfer.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.TransactionGuidGenerator;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBoxRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Transactional
@Component
public class ExternalDepositService {

    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;
    private final AccountRepository accountRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.create("http://localhost:8080");

    private final String externalTransferDepositSuccessCallbackUrl = "http://localhost:8080";

    public void store(ExternalDepositRequestDto externalDepositRequestDto) {
        ExternalTransferDepositOutBox outBox = toExternalTransferDepositOutBox(externalDepositRequestDto);
        externalTransferDepositOutBoxRepository.save(outBox);
    }

    public boolean execute(ExternalDepositRequestDto externalDepositRequestDto) {
        Account account = accountRepository.findByAccountNumberForUpdate(externalDepositRequestDto.getDepositAccountNumber())
                .orElseThrow(() -> new RuntimeException("deposit account doesn't exist"));

        BigDecimal depositAmountResult = deposit(externalDepositRequestDto, account);

//        saveTransferHistory(externalDepositRequestDto, depositAmountResult);

        // 입금 요청
        ExternalDepositSuccessRequestDto depositSuccessRequestDto = new ExternalDepositSuccessRequestDto(externalDepositRequestDto.getPublicTransferId(), externalTransferDepositSuccessCallbackUrl, true);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(depositSuccessRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parsing error");
        }

        Mono<ClientResponse> responseMono = webClient.post()
                .uri("/account/transfer/deposit/success/post")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchangeToMono(response -> Mono.just(response));

        HttpStatus status = responseMono.block().statusCode();
        if (!status.is2xxSuccessful()) {
            return false;
        }
        return true;
    }

    private void saveTransferHistory(ExternalDepositRequestDto externalDepositRequestDto, BigDecimal depositAmountResult) {
        TransferHistory transferHistory = toTransferHistory(externalDepositRequestDto, depositAmountResult);
        transferHistoryRepository.save(transferHistory);
    }

    private BigDecimal deposit(ExternalDepositRequestDto externalDepositRequestDto, Account account) {
        BigDecimal depositAmountResult = account.getBalance().add(externalDepositRequestDto.getTransferAmount());
        account.setBalance(depositAmountResult);
        return depositAmountResult;
    }

    private TransferHistory toTransferHistory(ExternalDepositRequestDto externalDepositRequestDto, BigDecimal amountAfterDeposit) {
        return new TransferHistory(
                new TransferHistory.DateAndGUID(LocalDate.now(), TransactionGuidGenerator.getGuid("01")),
                externalDepositRequestDto.getWithdrawalBankId(),
                externalDepositRequestDto.getWithdrawalAccountNumber(),
                externalDepositRequestDto.getWithdrawalMemberName(),
                externalDepositRequestDto.getAmountAfterWithdrawal(),
                externalDepositRequestDto.getMemoToSender(),
                externalDepositRequestDto.getDepositBankId(),
                externalDepositRequestDto.getDepositAccountNumber(),
                externalDepositRequestDto.getDepositMemberName(),
                externalDepositRequestDto.getAmountAfterDeposit(),
                externalDepositRequestDto.getMemoToReceiver(),
                externalDepositRequestDto.getTransferAmount(),
                externalDepositRequestDto.getCreateTime(),
                externalDepositRequestDto.getPublicTransferId(),
                TransferState.FINISHED
        );
    }

    private ExternalTransferDepositOutBox toExternalTransferDepositOutBox(ExternalDepositRequestDto externalDepositRequestDto) {
        try {
            String body = objectMapper.writeValueAsString(externalDepositRequestDto);
            return new ExternalTransferDepositOutBox(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Async("serviceAsyncExecutor")
    public void executeAllProcess(ExternalTransferOutBox outBox) {

        // 아웃 박스 select for update
        ExternalTransferOutBox outBoxForUpdate = externalTransferOutBoxRepository.findExternalTransferOutBoxForUpdate(outBox.getId())
                .orElseThrow(() -> new RuntimeException("OutBox doesn't exists"));

        TransferHistory transferHistory = null;
        try {
            transferHistory = objectMapper.readValue(outBoxForUpdate.getPayLoad(), TransferHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }


        // 입금 요청
        ExternalDepositRequestDto externalDepositRequestDto = ExternalDepositRequestDto.of(transferHistory);
        // 이체 입금 요청 API 호출

        // 아웃 박스 삭제
        externalTransferOutBoxRepository.delete(outBoxForUpdate);
    }
}
