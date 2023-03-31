package com.ms.bank.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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

    protected Account() {}
}
