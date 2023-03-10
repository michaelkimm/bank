package com.ms.bank.transfer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class TransferHistory {

    @EmbeddedId
    private DateAndGUID dateAndGUID;

    @Column(length = 2)
    String withdrawalBankId;

    @Column(length = 20)
    String withdrawalAccountNumber;

    @Column(length = 30)
    String withdrawalMemberName;

    @Column(length = 20)
    BigDecimal amountAfterWithdrawal;

    @Column(length = 10)
    String memoToSender;

    @Column(length = 2)
    String depositBankId;

    @Column(length = 20)
    String depositAccountNumber;

    @Column(length = 30)
    String depositMemberName;

    @Column(length = 20)
    BigDecimal amountAfterDeposit;

    @Column(length = 10)
    String memoToReceiver;

    @Column
    BigDecimal transferAmount;

    @Column
    LocalDateTime createTime;

    @Column(length = 40, unique = true)
    String publicTransferId;

    @Column
    TransferState state;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class DateAndGUID implements Serializable {

        @Column
        private LocalDate createDate;

        @Column
        private String guid;
    }
}

