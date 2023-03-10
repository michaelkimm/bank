package com.ms.bank.transfer.application;


import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.application.dto.TransferRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransferService {

    private final ExternalTransferService externalTransferService;
    private final InternalTransferService internalTransferService;


    public void execute(final TransferRequestDto requestDto) {

        if (isInternalTransfer(requestDto.getWithdrawalBankId(), requestDto.getDepositBankId())) {
            internalTransferService.executeTransfer(requestDto);
        } else {
            externalTransferService.executeTransfer(requestDto);
        }
    }

    public void processTransferDepositSuccess(final ExternalDepositSuccessRequestDto externalDepositSuccessRequestDto) {
        externalTransferService.processTransferDepositSuccess(externalDepositSuccessRequestDto);
    }

    private boolean isInternalTransfer(String fromBankId, String toBankId) {
        return fromBankId.equals(toBankId);
    }
}
