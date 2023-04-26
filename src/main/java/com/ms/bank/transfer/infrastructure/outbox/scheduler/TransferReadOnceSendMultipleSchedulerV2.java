package com.ms.bank.transfer.infrastructure.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import com.ms.bank.transfer.infrastructure.outbox.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ExternalTransferDepositSuccessResponseOutBoxRepository externalTransferDepositSuccessResponseOutBoxRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final Executor depositProcessAsyncExecutor;

    private final ObjectMapper objectMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Scheduled(fixedDelay = 100)
    public void processTransferOutBoxMessage() {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAllExternalTransferOutBoxForUpdate();
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
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Scheduled(fixedDelay = 100)
    public void processTransferDepositOutBoxMessage() {
        List<ExternalTransferDepositOutBox> outboxList = externalTransferDepositOutBoxRepository.findAllExternalTransferDepositOutBoxForUpdate();
        log.info("outboxList size: {}", outboxList.size());
        if (outboxList.isEmpty()) {
            return;
        }

        // 이체 입금 순차 처리
        List<CompletableFuture<Void>> transferDepositFutures = outboxList.stream()
                .map(this::getExternalDepositRequestDto)
                .map(externalDepositRequestDto -> CompletableFuture.runAsync(() -> externalDepositService.executeTransferDeposit(externalDepositRequestDto), depositProcessAsyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(transferDepositFutures.toArray(new CompletableFuture[0]));

        try {
            allFuture.join();
            // 이체 입금 이벤트 삭제, 이체 입금 완료 이벤트 적재
            if (allFuture.isDone()) {
                externalTransferDepositOutBoxRepository.deleteAll(outboxList);
                outboxList.stream()
                        .map(this::getExternalDepositRequestDto)
                        .map(this::toExternalTransferDepositSuccessResponseOutBox)
                        .forEach(externalTransferDepositSuccessResponseOutBoxRepository::save);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Deprecated
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

    private ExternalTransferDepositSuccessResponseOutBox toExternalTransferDepositSuccessResponseOutBox(ExternalDepositRequestDto externalDepositRequestDto) {

        try {
            String value = objectMapper.writeValueAsString(externalDepositRequestDto);
            return new ExternalTransferDepositSuccessResponseOutBox(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
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
