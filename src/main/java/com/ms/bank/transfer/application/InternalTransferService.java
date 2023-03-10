package com.ms.bank.transfer.application;

import com.ms.bank.transfer.application.dto.TransferRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class InternalTransferService {

    public void executeTransfer(TransferRequestDto transferRequestDto) {

    }
}
