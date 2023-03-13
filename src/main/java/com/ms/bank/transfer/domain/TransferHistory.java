package com.ms.bank.transfer.domain;

import com.ms.bank.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class TransferHistory extends BaseEntity {

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

    @Column(length = 40, unique = true)
    String publicTransferId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
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

    public TransferHistory(DateAndGUID dateAndGUID,
                           String withdrawalBankId,
                           String withdrawalAccountNumber,
                           String withdrawalMemberName,
                           BigDecimal amountAfterWithdrawal,
                           String memoToSender,
                           String depositBankId,
                           String depositAccountNumber,
                           String depositMemberName,
                           BigDecimal amountAfterDeposit,
                           String memoToReceiver,
                           BigDecimal transferAmount,
                           String publicTransferId,
                           TransferState state) {
        this.dateAndGUID = dateAndGUID;
        this.withdrawalBankId = withdrawalBankId;
        this.withdrawalAccountNumber = withdrawalAccountNumber;
        this.withdrawalMemberName = withdrawalMemberName;
        this.amountAfterWithdrawal = amountAfterWithdrawal;
        this.memoToSender = memoToSender;
        this.depositBankId = depositBankId;
        this.depositAccountNumber = depositAccountNumber;
        this.depositMemberName = depositMemberName;
        this.amountAfterDeposit = amountAfterDeposit;
        this.memoToReceiver = memoToReceiver;
        this.transferAmount = transferAmount;
        this.publicTransferId = publicTransferId;
        this.state = state;
    }
}

