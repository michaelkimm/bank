package com.ms.bank.transfer.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.application.dto.TransferRequestDto;
import com.ms.bank.transfer.domain.TransferHistory;
import com.ms.bank.transfer.domain.TransferState;
import com.ms.bank.transfer.infrastructure.PublicTransferIdGenerator;
import com.ms.bank.transfer.infrastructure.TransactionGuidGenerator;
import com.ms.bank.transfer.infrastructure.TransferHistoryRepository;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBox;
import com.ms.bank.transfer.infrastructure.outbox.ExternalTransferOutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Component
public class ExternalTransferService {

    private final AccountRepository accountRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final ExternalTransferOutBoxRepository externalTransferOutBoxRepository;

    private final String externalTransferId = "01";
    private final ObjectMapper objectMapper;

    public void executeTransfer(final TransferRequestDto transferRequestDto) {
        // 이체 전 검증 진행
        // 출금
        Account withdrawalAccount = accountRepository.findByAccountNumberForUpdate(transferRequestDto.getWithdrawalAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account doesn't exist"));

        if (!checkIfBalanceIsEnough(transferRequestDto, withdrawalAccount)) {
            throw new RuntimeException("Withdrawal account money is not enough");
        }
        BigDecimal amountAfterWithdrawal = withdrawalAccount.getBalance().subtract(transferRequestDto.getTransferAmount());
        withdrawalAccount.setBalance(amountAfterWithdrawal);

//         이체 내역(시작) 쌓기
        TransferHistory transferHistory = toTransferHistory(transferRequestDto, BigDecimal.ONE);
        transferHistoryRepository.save(transferHistory);

        // 이체 이벤트 쌓기
        ExternalTransferOutBox outBox = toExternalTransferOutBox(transferHistory);
        externalTransferOutBoxRepository.save(outBox);
    }

    private boolean checkIfBalanceIsEnough(TransferRequestDto transferRequestDto, Account withdrawalAccount) {
        if (withdrawalAccount.getBalance().compareTo(transferRequestDto.getTransferAmount()) >= 0) {
            return true;
        }
        return false;
    }

    private TransferHistory toTransferHistory(TransferRequestDto transferRequestDto, BigDecimal amountAfterWithdrawal) {
        return new TransferHistory(
                new TransferHistory.DateAndGUID(LocalDate.now(), TransactionGuidGenerator.getGuid(externalTransferId)),
                transferRequestDto.getWithdrawalBankId(),
                transferRequestDto.getWithdrawalAccountNumber(),
                transferRequestDto.getWithdrawalMemberName(),
                amountAfterWithdrawal,
                transferRequestDto.getMemoToSender(),
                transferRequestDto.getDepositBankId(),
                transferRequestDto.getDepositAccountNumber(),
                transferRequestDto.getDepositMemberName(),
                null,
                transferRequestDto.getMemoToReceiver(),
                transferRequestDto.getTransferAmount(),
                PublicTransferIdGenerator.getGuid(transferRequestDto.getWithdrawalBankId(), transferRequestDto.getDepositBankId(), externalTransferId),
                TransferState.CREATED
        );
    }

    private ExternalTransferOutBox toExternalTransferOutBox(TransferHistory transferHistory) {

        try {
            String value = objectMapper.writeValueAsString(transferHistory);
            return new ExternalTransferOutBox(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

