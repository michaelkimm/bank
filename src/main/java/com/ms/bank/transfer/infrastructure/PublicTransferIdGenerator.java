package com.ms.bank.transfer.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PublicTransferIdGenerator {

    private static AtomicInteger atomicInteger = new AtomicInteger();

    private static String serverId;

    @Value("${server.id}")
    public void setServerId(String value) {
        serverId = value;
    }

    public static String getGuid(String fromBankId, String toBankId, String transactionId) {
        StringBuilder sb = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sb.append(time);
        sb.append(serverId);
        sb.append(fromBankId);
        sb.append(toBankId);
        sb.append(transactionId);

        int val = atomicInteger.getAndIncrement();
        sb.append(String.format("%05d", val));
        return sb.toString();
    }
}
