package com.ms.bank.transfer.infrastructure;

import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Getter
public enum Bank {

    BANK1("01", "http://slb-17194192.ncloudslb.com"),
    BANK2("02", "http://slb-17194194.ncloudslb.com"),
//    BANK1("01", "http://localhost:8080"),
//    BANK2("02", "http://localhost:8080"),
    NO_BANK("00", "http://localhost:8080");

    private String bankId;
    private String host;
    private WebClient webClient;

    Bank(String bankId, String host) {
        this.bankId = bankId;
        this.host = host;
        this.webClient = WebClient.create(host);
    }

    public static Bank findByBankId(String bankId) {
        return Arrays.stream(Bank.values())
                .filter(bank -> bank.bankId.equals(bankId))
                .findFirst()
                .orElse(NO_BANK);
    }
}
