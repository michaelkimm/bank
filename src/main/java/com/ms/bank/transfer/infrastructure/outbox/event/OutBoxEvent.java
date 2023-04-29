package com.ms.bank.transfer.infrastructure.outbox.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutBoxEvent {

    private final String aggregateId;
    private final String aggregateType;
    private final String eventType;
    private final String payload;
}
