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

import java.util.LinkedList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferReadOnceSendMultipleScheduler {

    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    private final ObjectMapper objectMapper;

    @Deprecated
    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
//    @Scheduled(fixedDelay = 100)
    public void processTransferOutBoxMessage() {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAllExternalTransferOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }

        List<Long> outBoxCompletedList = new LinkedList<>();
        outboxList.forEach(outBox -> {
            TransferHistory transferHistory = getTransferHistory(outBox);
            ExternalDepositRequestDto externalDepositRequestDto = ExternalDepositRequestDto.of(transferHistory);

            try {
                externalDepositService.callExternalDepositRequest(externalDepositRequestDto);
                outBoxCompletedList.add(outBox.getId());
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        });

        if (!outBoxCompletedList.isEmpty()) {
            outboxList.stream()
                    .map(this::getTransferHistory)
                    .forEach(this::processTransferDeposit);
            externalTransferOutBoxRepository.deleteAllById(outBoxCompletedList);
        }
    }

    @Deprecated
    @Transactional
//    @Async("transferSchedulerAsyncExecutor")
//    @Scheduled(fixedDelay = 100)
    public void processTransferDepositOutBoxMessage() {

        List<ExternalTransferDepositOutBox> outboxList = externalTransferDepositOutBoxRepository.findAllExternalTransferDepositOutBoxForUpdate();
        if (outboxList.isEmpty()) {
            return;
        }

        List<Long> outBoxCompletedList = new LinkedList<>();
        outboxList.forEach(outBox -> {
            ExternalDepositRequestDto externalDepositRequestDto = getExternalDepositRequestDto(outBox);

            externalDepositService.executeTransferDeposit(externalDepositRequestDto);
            boolean result = externalDepositService.executeSuccessProcess(externalDepositRequestDto);
            if (result) {
                outBoxCompletedList.add(outBox.getId());
            }
        });

        if (!outBoxCompletedList.isEmpty()) {
            externalTransferDepositOutBoxRepository.deleteAllById(outBoxCompletedList);
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
