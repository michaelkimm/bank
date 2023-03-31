package com.ms.bank.transfer.application;

import com.ms.bank.account.Account;
import com.ms.bank.account.AccountRepository;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.domain.TransferState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExternalDepositServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExternalDepositService externalDepositService;

    @Transactional
    @DisplayName("이체입금요청_성공")
    @Test
    void executeTransferDeposit() throws InterruptedException {
        int threadCount = 100;
        BigDecimal transferValue = BigDecimal.ONE;

        Account depositAccount = new Account("2222", BigDecimal.valueOf(0));
        accountRepository.save(depositAccount);

        ExternalDepositRequestDto externalDepositRequestDto = new ExternalDepositRequestDto(
                LocalDate.now(),
                "01",
                "1111",
                "gallais",
                BigDecimal.ZERO,
                "im sender",
                "02",
                depositAccount.getAccountNumber(),
                "jack",
                BigDecimal.ZERO,
                "im receiver",
                transferValue,
                LocalDateTime.now(),
                LocalDateTime.now(),
                String.valueOf(0),
                TransferState.FINISHED
        );
        externalDepositService.executeTransferDeposit(externalDepositRequestDto);

        depositAccount = accountRepository.findById(depositAccount.getAccountNumber()).orElseThrow();

        // 100 - (100 * 1) = 0
        assertEquals(transferValue, depositAccount.getBalance());
    }

    @DisplayName("이체입금요청_성공_동시에_100명이_요청")
    @Test
    void executeTransferDeposit_multi_thread() throws InterruptedException {
        int threadCount = 100;
        BigDecimal transferValue = BigDecimal.ONE;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Account depositAccount = new Account("2222", BigDecimal.valueOf(0));
        accountRepository.save(depositAccount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            Account finalDepositAccount = depositAccount;
            executorService.submit(() -> {
                try {
                    ExternalDepositRequestDto externalDepositRequestDto = new ExternalDepositRequestDto(
                            LocalDate.now(),
                            "01",
                            "1111",
                            "gallais",
                            BigDecimal.ZERO,
                            "im sender",
                            "02",
                            finalDepositAccount.getAccountNumber(),
                            "jack",
                            BigDecimal.ZERO,
                            "im receiver",
                            transferValue,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            String.valueOf(finalI),
                            TransferState.FINISHED
                    );

                    externalDepositService.executeTransferDeposit(externalDepositRequestDto);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        depositAccount = accountRepository.findById(depositAccount.getAccountNumber()).orElseThrow();

        // 100 - (100 * 1) = 0
        assertEquals(BigDecimal.valueOf(100L).longValue(), depositAccount.getBalance().longValue());
    }
}