package com.ms.bank.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Account {

    @Id
    @Column(length = 20)
    private String accountNumber;

    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version = 1L;

    protected Account() {}

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
