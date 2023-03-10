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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferReadOnceSendMultipleSchedulerV2 {

    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;
    private final Executor serviceAsyncExecutor;

    private WebClient webClient = WebClient.create("http://localhost:8080");
    private final ObjectMapper objectMapper;

    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
    @Scheduled(fixedDelay = 100)
    public void processTransferOutBoxMessage() {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAllExternalTransferOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> transferDepositFutures = outboxList.stream()
                .map(this::getTransferHistory)
                .map(ExternalDepositRequestDto::of)
                .map(externalDepositRequestDto -> CompletableFuture.runAsync(() -> externalDepositService.callExternalDepositRequest(externalDepositRequestDto), serviceAsyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(transferDepositFutures.toArray(new CompletableFuture[0]));

        try {
            allFuture.join();
            if (allFuture.isDone()) {
                externalTransferOutBoxRepository.deleteAll(outboxList);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
    @Scheduled(fixedDelay = 100)
    public void processTransferDepositOutBoxMessage() {

        List<ExternalTransferDepositOutBox> outboxList = externalTransferDepositOutBoxRepository.findAllExternalTransferDepositOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }

        List<CompletableFuture<Boolean>> transferDepositFutures = outboxList.stream()
                .map(this::getExternalDepositRequestDto)
                .map(externalDepositRequestDto -> CompletableFuture.supplyAsync(() -> externalDepositService.executeSuccessProcess(externalDepositRequestDto), serviceAsyncExecutor))
                .collect(Collectors.toList());

        List<Boolean> transferDepositResults = transferDepositFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        if (!transferDepositResults.contains(false)) {
            externalTransferDepositOutBoxRepository.deleteAll(outboxList);
        }
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
