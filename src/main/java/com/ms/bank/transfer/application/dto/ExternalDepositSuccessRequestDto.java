package com.ms.bank.transfer.application.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExternalDepositSuccessRequestDto {

    String publicTransferId;
    String callbackUrl;
    boolean success;
}
