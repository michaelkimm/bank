package com.ms.bank.transfer.application;

import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepositService {

    private final AccountRepository accountRepository;

    @Transactional
    public void deposit(String depositAccountNumber, BigDecimal amount) {
        // 입금 진행
        Account depositAccount = accountRepository.findByAccountNumberForUpdate(depositAccountNumber).get();

        BigDecimal depositResult = depositAccount.getBalance().add(amount);
        depositAccount.setBalance(depositResult);
    }
}
