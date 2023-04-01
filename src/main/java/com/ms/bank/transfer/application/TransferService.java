package com.ms.bank.transfer.application;


import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.application.dto.TransferRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransferService {

    private final ExternalTransferService externalTransferService;
    private final InternalTransferService internalTransferService;
    private final TransferHistoryRepository transferHistoryRepository;
    private final AccountRepository accountRepository;


    public void execute(final TransferRequestDto requestDto) {

//        if (isInternalTransfer(requestDto.getWithdrawalBankId(), requestDto.getDepositBankId())) {
//            internalTransferService.executeTransfer(requestDto);
//        } else {
//            externalTransferService.executeTransfer(requestDto);
//        }
    }

    @Transactional
    public void processTransferDepositSuccess(final ExternalDepositSuccessRequestDto externalDepositSuccessRequestDto) {
        TransferHistory transferHistory = transferHistoryRepository.findTransferHistoryByPublicTransferId(externalDepositSuccessRequestDto.getPublicTransferId())
                .orElseThrow(() -> new RuntimeException("transfer history doesn't exist"));
        if (!externalDepositSuccessRequestDto.isSuccess()) {
            // 롤백
            transferHistory.setState(TransferState.CANCELED);
            depositToWithdrawalAccount(transferHistory);
        } else {
            transferHistory.setState(TransferState.FINISHED);
        }
    }

    private void depositToWithdrawalAccount(TransferHistory transferHistory) {
        Account withdrawalAccount = accountRepository.findByAccountNumberForUpdate(transferHistory.getWithdrawalAccountNumber())
                .orElseThrow(() -> new RuntimeException("Withdrawal account does not exist"));
        withdrawalAccount.setBalance(withdrawalAccount.getBalance().add(transferHistory.getTransferAmount()));
    }

    private boolean isInternalTransfer(String fromBankId, String toBankId) {
        return fromBankId.equals(toBankId);
    }
}
