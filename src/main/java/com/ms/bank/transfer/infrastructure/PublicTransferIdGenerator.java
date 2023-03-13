package com.ms.bank.transfer.infrastructure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class PublicTransferIdGenerator {

    private static AtomicInteger atomicInteger = new AtomicInteger();

    private static final String serverIdHashed = "20";

    public static String getGuid(String fromBankId, String toBankId, String transactionId) {
        StringBuilder sb = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sb.append(time);
        sb.append(serverIdHashed);
        sb.append(fromBankId);
        sb.append(toBankId);
        sb.append(transactionId);

        int val = atomicInteger.getAndIncrement();
        sb.append(String.format("%05d", val));
        return sb.toString();
    }
}
