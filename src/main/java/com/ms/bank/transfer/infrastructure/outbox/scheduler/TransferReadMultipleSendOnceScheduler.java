package com.ms.bank.transfer.infrastructure.outbox.scheduler;

import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferDepositOutBoxRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferReadMultipleSendOnceScheduler {
    private final ExternalDepositService externalDepositService;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;
    private final ExternalTransferDepositOutBoxRepository externalTransferDepositOutBoxRepository;

    @Scheduled(fixedDelay = 1000)
    public void processTransferOutBoxMessage() {

        List<ExternalTransferOutBox> outboxList = externalTransferOutBoxRepository.findAll();
        if (outboxList.isEmpty()) {
            return;
        }

        outboxList.stream()
                .forEach(externalDepositService::executeAllRequestProcess);

    }

    @Scheduled(fixedDelay = 1000)
    public void processTransferDepositOutBoxMessage() {

        List<ExternalTransferDepositOutBox> outboxList = externalTransferDepositOutBoxRepository.findAll();
        if (outboxList.isEmpty()) {
            return;
        }

        outboxList.stream()
                .forEach(externalDepositService::executeAllSuccessProcess);
    }
}
