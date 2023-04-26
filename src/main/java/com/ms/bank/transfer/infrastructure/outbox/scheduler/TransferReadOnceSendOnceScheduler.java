package com.ms.bank.transfer.infrastructure.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBoxRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferReadOnceSendOnceScheduler {

    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;

    private WebClient webClient = WebClient.create("http://localhost:8080");
    private final ObjectMapper objectMapper;

    @Deprecated
    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
//    @Scheduled(fixedDelay = 10)
    public void processTransferOutBoxMessage() {

        Optional<ExternalTransferOutBox> outbox = externalTransferOutBoxRepository.findOneForUpdate();
        if (outbox.isEmpty()) {
            return;
        }
        ExternalTransferOutBox outBoxForUpdate = outbox.get();

        TransferHistory transferHistory = getTransferHistory(outBoxForUpdate);

        // 입금 요청
        ExternalDepositRequestDto externalDepositRequestDto = ExternalDepositRequestDto.of(transferHistory);
        externalDepositService.callExternalDepositRequest(externalDepositRequestDto);

        // 아웃 박스 삭제
        externalTransferOutBoxRepository.delete(outBoxForUpdate);
    }

    @Deprecated
    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
//    @Scheduled(fixedDelay = 10)
    public void processTransferDepositOutBoxMessage() {

        Optional<ExternalTransferDepositOutBox> outbox = externalTransferDepositOutBoxRepository.findOneForUpdate();
        if (outbox.isEmpty()) {
            return;
        }
        ExternalTransferDepositOutBox outBoxForUpdate = outbox.get();

        ExternalDepositRequestDto externalDepositRequestDto = getExternalDepositRequestDto(outBoxForUpdate);

        // 입금 처리
        externalDepositService.executeTransferDeposit(externalDepositRequestDto);
        boolean result = externalDepositService.executeSuccessProcess(externalDepositRequestDto);
        if (!result) {
            return;
        }

        // 아웃 박스 삭제
        externalTransferDepositOutBoxRepository.delete(outBoxForUpdate);
    }

    private TransferHistory getTransferHistory(ExternalTransferOutBox outBoxForUpdate) {
        TransferHistory transferHistory = null;
        try {
            transferHistory = objectMapper.readValue(outBoxForUpdate.getPayLoad(), TransferHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return transferHistory;
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
}
