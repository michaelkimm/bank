package com.ms.bank.transfer.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.Bank;
import com.ms.bank.transfer.infrastructure.TransactionGuidGenerator;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import com.ms.bank.transfer.infrastructure.outbox.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Component
public class ExternalDepositService {

    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;
    private final ExternalTransferDepositSuccessResponseOutBoxRepository externalTransferDepositSuccessResponseOutBoxRepository;
    private final AccountRepository accountRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    private final ObjectMapper objectMapper;

    public void store(ExternalDepositRequestDto externalDepositRequestDto) {
        ExternalTransferDepositOutBox outBox = toExternalTransferDepositOutBox(externalDepositRequestDto);
        externalTransferDepositOutBoxRepository.save(outBox);
    }

//    @Async
    public void executeTransferDeposit(ExternalDepositRequestDto externalDepositRequestDto) {
//        Account account = accountRepository.findByAccountNumberForUpdate(externalDepositRequestDto.getDepositAccountNumber())
//                .orElseThrow(() -> new RuntimeException("deposit account doesn't exist"));

        Account account = accountRepository.findByAccountNumber(externalDepositRequestDto.getDepositAccountNumber())
                .orElseThrow(() -> new RuntimeException("haha"));
//        log.info("got lock");

        BigDecimal depositAmountResult = deposit(externalDepositRequestDto, account);

        saveTransferHistory(externalDepositRequestDto, depositAmountResult);

        ExternalTransferDepositSuccessResponseOutBox outBox = toExternalTransferDepositSuccessResponseOutBox(externalDepositRequestDto);
        externalTransferDepositSuccessResponseOutBoxRepository.save(outBox);
    }

    public boolean executeSuccessProcess(ExternalDepositRequestDto externalDepositRequestDto) {

        // 입금 완료 응답 보내기
        Bank withdrawalBank = Bank.findByBankId(externalDepositRequestDto.getWithdrawalBankId());
        ExternalDepositSuccessRequestDto depositSuccessRequestDto = new ExternalDepositSuccessRequestDto(externalDepositRequestDto.getPublicTransferId(), withdrawalBank.getHost(), true);
        boolean result = callExternalDepositSuccessRequest(depositSuccessRequestDto);

        return result;
    }

    @Async("depositSuccessProcessAsyncExecutor")
    public void executeAllSuccessProcess(ExternalTransferDepositOutBox externalTransferDepositOutBox) {
        // 아웃 박스 lock 걸고 조회
        Optional<ExternalTransferDepositOutBox> outBoxForUpdate = externalTransferDepositOutBoxRepository.findExternalTransferDepositOutBoxForUpdate(externalTransferDepositOutBox.getId());
        if (outBoxForUpdate.isEmpty()) {
            return;
        }
        externalTransferDepositOutBox = outBoxForUpdate.get();
        ExternalDepositRequestDto externalDepositRequestDto = getExternalDepositRequestDto(externalTransferDepositOutBox);

        // 입금 진행
        executeTransferDeposit(externalDepositRequestDto);
        
        // 입금 완료 응답 보내기
        executeSuccessProcess(externalDepositRequestDto);

        // 아웃 박스 삭제
        externalTransferDepositOutBoxRepository.delete(externalTransferDepositOutBox);
    }

    private ExternalDepositRequestDto getExternalDepositRequestDto(ExternalTransferDepositOutBox outBoxForUpdate) {
        ExternalDepositRequestDto externalDepositRequestDto = null;
        try {
            externalDepositRequestDto = objectMapper.readValue(outBoxForUpdate.getPayLoad(), ExternalDepositRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return externalDepositRequestDto;
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

    @Async("depositProcessAsyncExecutor")
    public void executeAllRequestProcess(ExternalTransferOutBox outBox) {

        // 아웃 박스 select for update
        Optional<ExternalTransferOutBox> outBoxForUpdate = externalTransferOutBoxRepository.findExternalTransferOutBoxForUpdate(outBox.getId());
        if (outBoxForUpdate.isEmpty()) {
            return;
        }
        outBox = outBoxForUpdate.get();
        TransferHistory transferHistory = getTransferHistory(outBox);

        // 입금 요청
        ExternalDepositRequestDto externalDepositRequestDto = ExternalDepositRequestDto.of(transferHistory);
        // 이체 입금 요청 API 호출
        callExternalDepositRequest(externalDepositRequestDto);

        // 아웃 박스 삭제
        externalTransferOutBoxRepository.delete(outBox);
    }

    private TransferHistory getTransferHistory(ExternalTransferOutBox outBox) {
        TransferHistory transferHistory = null;
        try {
            transferHistory = objectMapper.readValue(outBox.getPayLoad(), TransferHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return transferHistory;
    }

    public void callExternalDepositRequest(ExternalDepositRequestDto externalDepositRequestDto) {
        String body = null;
        try {
            body = objectMapper.writeValueAsString(externalDepositRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parsing error");
        }
        Bank depositBank = Bank.findByBankId(externalDepositRequestDto.getDepositBankId());
        WebClient webClient = depositBank.getWebClient();

        Mono<ClientResponse> responseMono = webClient.post()
                .uri("/account/transfer/deposit/post")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchangeToMono(response -> Mono.just(response));

        HttpStatus status = responseMono.block().statusCode();
        if (!status.is2xxSuccessful()) {
            throw new RuntimeException("http response is not 200");
        }
    }

    private boolean callExternalDepositSuccessRequest(ExternalDepositSuccessRequestDto externalDepositSuccessRequestDto) {
        String body = null;
        try {
            body = objectMapper.writeValueAsString(externalDepositSuccessRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parsing error");
        }

        WebClient webClient = WebClient.create(externalDepositSuccessRequestDto.getCallbackUrl());

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

    private ExternalTransferDepositSuccessResponseOutBox toExternalTransferDepositSuccessResponseOutBox(ExternalDepositRequestDto externalDepositRequestDto) {

        try {
            String value = objectMapper.writeValueAsString(externalDepositRequestDto);
            return new ExternalTransferDepositSuccessResponseOutBox(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
