package com.ms.bank.transfer.infrastructure.outbox.event;

import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ExternalTransferWithdrawalEvent {

    private String withdrawalBankId;
    private String withdrawalAccountNumber;
    private String withdrawalMemberName;
    private BigDecimal amountAfterWithdrawal;
    private String memoToSender;
    private String depositBankId;
    private String depositAccountNumber;
    private String depositMemberName;
    private BigDecimal amountAfterDeposit;
    private String memoToReceiver;
    private BigDecimal transferAmount;
    private String publicTransferId;
    private TransferState state;

    public static ExternalTransferWithdrawalEvent of (TransferHistory transferHistory) {
        return new ExternalTransferWithdrawalEvent(
                transferHistory.getWithdrawalBankId(),
                transferHistory.getWithdrawalAccountNumber(),
                transferHistory.getWithdrawalMemberName(),
                transferHistory.getAmountAfterWithdrawal(),
                transferHistory.getMemoToSender(),
                transferHistory.getDepositBankId(),
                transferHistory.getDepositAccountNumber(),
                transferHistory.getDepositMemberName(),
                transferHistory.getAmountAfterDeposit(),
                transferHistory.getMemoToReceiver(),
                transferHistory.getTransferAmount(),
                transferHistory.getPublicTransferId(),
                transferHistory.getState()
        );
    }

    public static TransferHistory toTransferHistory (ExternalTransferWithdrawalEvent event) {
        return new TransferHistory(
                event.getWithdrawalBankId(),
                event.getWithdrawalAccountNumber(),
                event.getWithdrawalMemberName(),
                event.getAmountAfterWithdrawal(),
                event.getMemoToSender(),
                event.getDepositBankId(),
                event.getDepositAccountNumber(),
                event.getDepositMemberName(),
                event.getAmountAfterDeposit(),
                event.getMemoToReceiver(),
                event.getTransferAmount(),
                event.getPublicTransferId(),
                event.getState()
        );
    }
}
