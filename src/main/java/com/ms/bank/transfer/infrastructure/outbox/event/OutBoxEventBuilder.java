package com.ms.bank.transfer.infrastructure.outbox.event;


public interface OutBoxEventBuilder<T> {

    OutBoxEvent createOutBoxEvent(T domainEvent);
}
