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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferReadOnceSendMultipleSchedulerV2 {

    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;
    private final ExternalTransferDepositSuccessResponseOutBoxRepository externalTransferDepositSuccessResponseOutBoxRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final Executor transferDepositRequestAsyncExecutor;
    private final Executor transferDepositProcessAsyncExecutor;
    private final Executor transferDepositSuccessResponseAsyncExecutor;

    private final ObjectMapper objectMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Scheduled(fixedRate = 10000)
    public void processTransferOutBoxMessage() {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAllExternalTransferOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }
        log.info("1 TransferOutBoxCnt: " + String.valueOf(outboxList.size()));

        LinkedList<CompletableFuture<Void>> futureLinkedList = new LinkedList<>();
        for (ExternalTransferOutBox outBox : outboxList) {
            TransferHistory transferHistory = getTransferHistory(outBox);
            ExternalDepositRequestDto requestDto = ExternalDepositRequestDto.of(transferHistory);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> externalDepositService.callExternalDepositRequest(requestDto), transferDepositRequestAsyncExecutor);
            futureLinkedList.add(future);
        }

        try {
            int futureCnt = futureLinkedList.size();
            for (CompletableFuture<Void> future : futureLinkedList) {
                future.join();
                futureCnt -= 1;
            }
            // is done 추가 필요
            
            if (futureCnt == 0) {
                externalTransferOutBoxRepository.deleteAll(outboxList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
//        log.info("1 TransferOutBox end");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Scheduled(fixedRate = 10000)
    public void processTransferDepositOutBoxMessage() {
        List<ExternalTransferDepositOutBox> outboxList = externalTransferDepositOutBoxRepository.findAllExternalTransferDepositOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }
        log.info("2 TransferDepositOutBoxCnt: " + String.valueOf(outboxList.size()));

        // 이체 입금 처리
        LinkedList<CompletableFuture<Void>> futureLinkedList = new LinkedList<>();
        for (ExternalTransferDepositOutBox outBox : outboxList) {
            ExternalDepositRequestDto depositRequestDto = ExternalDepositRequestDto.of(outBox);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> externalDepositService.executeTransferDeposit(depositRequestDto), transferDepositProcessAsyncExecutor);
            futureLinkedList.add(future);
        }

        try {
            int futureCnt = futureLinkedList.size();
            for (CompletableFuture<Void> future : futureLinkedList) {
                future.join();
                futureCnt -= 1;
            }
            
            // is done 추가 필요
            // 이체 입금 이벤트 삭제, 이체 입금 완료 이벤트 적재
            if (futureCnt == 0) {
                externalTransferDepositOutBoxRepository.deleteAll(outboxList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
//        log.info("2 TransferDepositOutBox end");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Scheduled(fixedRate = 10000)
    public void processTransferDepositSuccessResponseOutBoxMessage() {
        List<ExternalTransferDepositSuccessResponseOutBox> outboxList = externalTransferDepositSuccessResponseOutBoxRepository.findAllExternalTransferDepositSuccessResponseOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }
        log.info("3 TransferDepositSuccessOutBoxCnt: " + String.valueOf(outboxList.size()));

        LinkedList<CompletableFuture<Boolean>> futureLinkedList = new LinkedList<>();
        for (ExternalTransferDepositSuccessResponseOutBox outBox : outboxList) {
            ExternalDepositRequestDto requestDto = ExternalDepositRequestDto.of(outBox);
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> externalDepositService.executeSuccessProcess(requestDto), transferDepositSuccessResponseAsyncExecutor);
            futureLinkedList.add(future);
        }

        try {
            int futureCnt = futureLinkedList.size();
            for (CompletableFuture<Boolean> future : futureLinkedList) {
                future.join();
                futureCnt -= 1;
            }
            // is done 추가 필요

            if (futureCnt == 0) {
                externalTransferDepositSuccessResponseOutBoxRepository.deleteAll(outboxList);
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

//    private ExternalTransferDepositSuccessResponseOutBox toExternalTransferDepositSuccessResponseOutBox(ExternalDepositRequestDto externalDepositRequestDto) {
//
//        try {
//            String value = objectMapper.writeValueAsString(externalDepositRequestDto);
//            return new ExternalTransferDepositSuccessResponseOutBox(value);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }
}
