package com.ms.bank.transfer.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
public class TransferRequestDto {
    private String withdrawalBankId;
    private String withdrawalAccountNumber;
    private String withdrawalMemberName;
    private String memoToSender;
    private String depositBankId;
    private String depositAccountNumber;
    private String depositMemberName;
    private String memoToReceiver;
    private BigDecimal transferAmount;
}
