package com.ms.bank.transfer.infrastructure.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBoxRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final TransferHistoryRepository transferHistoryRepository;
    private final Executor depositProcessAsyncExecutor;

    private final ObjectMapper objectMapper;

    @Transactional
//    @Scheduled(fixedDelay = 100)
    public void processTransferOutBoxMessage() throws InterruptedException {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAllExternalTransferOutBoxForUpdate();

//        Thread.sleep(1000000);
        if (outboxList.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> transferDepositFutures = outboxList.stream()
                .map(this::getTransferHistory)
                .map(ExternalDepositRequestDto::of)
                .map(externalDepositRequestDto -> CompletableFuture.runAsync(() -> externalDepositService.callExternalDepositRequest(externalDepositRequestDto), depositProcessAsyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(transferDepositFutures.toArray(new CompletableFuture[0]));

        try {
            allFuture.join();
            if (allFuture.isDone()) {
                externalTransferOutBoxRepository.deleteAll(outboxList);
                outboxList.stream()
                        .map(this::getTransferHistory)
                        .forEach(this::processTransferDeposit);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void processTransferDeposit(TransferHistory transferHistory) {
        transferHistory = transferHistoryRepository.findTransferHistoryByPublicTransferId(transferHistory.getPublicTransferId())
                .orElseThrow(() -> new RuntimeException("transfer history does not exist"));
        transferHistory.setState(TransferState.FINISHED);
        transferHistoryRepository.save(transferHistory);
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
}
