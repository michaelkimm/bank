package com.ms.bank.transfer.infrastructure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionGuidGenerator {

    private static AtomicInteger atomicInteger = new AtomicInteger();

    private static final String serverId = "01";

    public static String getGuid(String transactionId) {
        StringBuilder sb = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sb.append(time);
        sb.append(serverId);
        sb.append(transactionId);

        int val = atomicInteger.getAndIncrement();
        sb.append(String.format("%05d", val));
        return sb.toString();
    }
}
