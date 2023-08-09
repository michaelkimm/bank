package com.ms.bank.transfer.application;

import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.TransferRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.TransactionGuidGenerator;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class InternalTransferService {

    private final TransferHistoryRepository transferHistoryRepository;
    private final AccountRepository accountRepository;

    /**
     * 혹시나 싶었는데 역시나 했네요..!
     * 본 프로젝트는 장애 상황에서 분산 트랜잭션의 데이터 일관성을 지키는 '다른 은행으로 돈을 이체하는 기능'에 초점을 뒀습니다.
     * 이에 같은 은행으로 돈을 이체하는 기능은 개발하지 않았었네요.
     * 아래 코드는 인턴 진행할 때 작성했던 코드입니다!
     * */

    public void executeTransfer(TransferRequestDto transferRequestDto) {
        String withdrawalAccountNumber = transferRequestDto.getWithdrawalAccountNumber();
        String depositAccountNumber = transferRequestDto.getDepositAccountNumber();

        if (withdrawalAccountNumber.compareTo(depositAccountNumber) == -1) {
            // 계좌 번호가 작은 쪽에서 먼저 비관락 획득
            Account withdrawalAccount = getAccount(withdrawalAccountNumber);
            withdrawalAccount.withdraw(transferRequestDto.getTransferAmount());

            Account depositAccount = getAccount(depositAccountNumber);
            depositAccount.deposit(transferRequestDto.getTransferAmount());

            saveTransferHistory(transferRequestDto, withdrawalAccount.getBalance(), depositAccount.getBalance());
        } else {
            Account depositAccount = getAccount(depositAccountNumber);
            depositAccount.deposit(transferRequestDto.getTransferAmount());

            Account withdrawalAccount = getAccount(withdrawalAccountNumber);
            withdrawalAccount.withdraw(transferRequestDto.getTransferAmount());

            saveTransferHistory(transferRequestDto, withdrawalAccount.getBalance(), depositAccount.getBalance());
        }
    }

    private void saveTransferHistory(TransferRequestDto transferRequestDto, BigDecimal amountAfterWithdrawal, BigDecimal amountAfterDeposit) {
        String guid = TransactionGuidGenerator.getGuid("01");
        TransferHistory transferHistory = new TransferHistory(
                new TransferHistory.DateAndGUID(LocalDate.now(), guid),
                transferRequestDto.getWithdrawalBankId(),
                transferRequestDto.getWithdrawalAccountNumber(),
                transferRequestDto.getWithdrawalMemberName(),
                amountAfterWithdrawal,
                transferRequestDto.getMemoToSender(),
                transferRequestDto.getDepositBankId(),
                transferRequestDto.getDepositAccountNumber(),
                transferRequestDto.getDepositMemberName(),
                amountAfterDeposit,
                transferRequestDto.getMemoToReceiver(),
                transferRequestDto.getTransferAmount(),
                guid,
                TransferState.FINISHED
        );
        transferHistoryRepository.save(transferHistory);
    }

    private Account getAccount(String withdrawalAccountNumber) {
        return accountRepository.findByAccountNumberForUpdate(withdrawalAccountNumber)
                .orElseThrow(IllegalAccessError::new);
    }
}
