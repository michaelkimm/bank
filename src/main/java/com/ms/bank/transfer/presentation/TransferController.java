package com.ms.bank.transfer.presentation;

import com.ms.bank.transfer.application.ExternalDepositService;
import com.ms.bank.transfer.application.TransferService;
import com.ms.bank.transfer.application.dto.ExternalDepositRequestDto;
import com.ms.bank.transfer.application.dto.ExternalDepositSuccessRequestDto;
import com.ms.bank.transfer.application.dto.TransferRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TransferController {

    private final TransferService transferService;
    private final ExternalDepositService externalDepositService;

    @Value("spring.datasource.url")
    private String url;

    @GetMapping("/account/transfer/get")
    ResponseEntity<Void> getTransferHistory() {
        log.info("transfer-get");
        log.info("aa : " + url);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/account/transfer/post")
    ResponseEntity<Void> transfer(@RequestBody TransferRequestDto transferRequestDto) {
        transferService.execute(transferRequestDto);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/account/transfer/deposit/post")
    ResponseEntity<Void> transferDeposit(@RequestBody ExternalDepositRequestDto externalDepositRequestDto) {
        externalDepositService.store(externalDepositRequestDto);
//        externalDepositService.executeTransferDeposit(externalDepositRequestDto);
        return ResponseEntity
                .ok()
                .build();
    }


    @PostMapping("/account/transfer/deposit/success/post")
    ResponseEntity<Void> transferDepositSuccess(@RequestBody ExternalDepositSuccessRequestDto externalDepositRequestDto) {
        log.info("deposit success posted");
        transferService.processTransferDepositSuccess(externalDepositRequestDto);
        return ResponseEntity
                .ok()
                .build();
    }
}
