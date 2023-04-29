package com.ms.bank.transfer.infrastructure.outbox.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.ms.bank.common.utils.ObjectMapperUtil;
import com.ms.bank.transfer.domain.TransferHistory;

public class ExternalTransferWithdrawalEventBuilder implements OutBoxEventBuilder<ExternalTransferWithdrawalEvent>{
    @Override
    public OutBoxEvent createOutBoxEvent(ExternalTransferWithdrawalEvent domainEvent) {

        JsonNode jsonNode = ObjectMapperUtil.getMapper().convertValue(domainEvent, JsonNode.class);

        return new OutBoxEvent(
                domainEvent.getPublicTransferId(),
                TransferHistory.class.getSimpleName(),
                domainEvent.getClass().getSimpleName(),
                jsonNode.toString()
        );
    }
}
