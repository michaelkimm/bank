package com.ms.bank.transfer.infrastructure;

import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Getter
public enum Bank {

//    BANK1("01", "http://27.96.135.122:8080"),
    BANK1("01", "http://223.130.132.229:8080"),
    BANK2("02", "http://223.130.132.229:8080"),
//    BANK2("02", "http://49.50.161.86:8080"),
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
