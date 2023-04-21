package com.ms.bank.account;

import com.ms.bank.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member owner;

    protected Account() {}
}
