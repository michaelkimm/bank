package com.ms.bank.transfer.application.dto;

import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ExternalDepositRequestDto {

    private LocalDate createDate;
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
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private String publicTransferId;
    private TransferState state;

    public static ExternalDepositRequestDto of(TransferHistory transferHistory) {
        return new ExternalDepositRequestDto(
                transferHistory.getDateAndGUID().getCreateDate(),
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
                transferHistory.getCreatedAt(),
                transferHistory.getUpdatedAt(),
                transferHistory.getPublicTransferId(),
                transferHistory.getState()
                );
    }
}
