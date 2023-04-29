package com.ms.bank.transfer.infrastructure.outbox.event;

import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import com.ms.bank.transfer.infrastructure.outbox.OutBox;
import com.ms.bank.transfer.infrastructure.outbox.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutBoxEventHandler {

    private final OutBoxRepository outBoxRepository;

    @EventListener
    public void handleOutBoxEvent(OutBoxEvent event) {
        outBoxRepository.save(
                OutBox.builder()
                        .aggregateId(event.getAggregateId())
                        .eventType(event.getEventType())
                        .aggregateType(event.getAggregateType())
                        .payload(event.getPayload())
                        .build()
        );
    }
}
