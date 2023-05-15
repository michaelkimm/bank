package com.ms.bank.transfer.infrastructure.outbox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.concurrent.atomic.AtomicInteger;

//@TableGenerator(
//        name = "EXTERNAL_TRANSFER_DEPOSIT_SUCCESS_RESPONSE_OUTBOX_SEQ_GENERATOR",
//        table = "CUSTOM_SEQUENCE"
//        , pkColumnValue = "EXTERNAL_TRANSFER_DEPOSIT_SUCCESS_RESPONSE_OUTBOX_SEQ"
//        , allocationSize = 1
//)
@NoArgsConstructor
@Getter
@Entity
public class ExternalTransferDepositSuccessResponseOutBox {

//    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "EXTERNAL_TRANSFER_DEPOSIT_SUCCESS_RESPONSE_OUTBOX_SEQ_GENERATOR")
private static AtomicInteger atomicInteger = new AtomicInteger();

    @Id
    Long id;

    @Lob
    @Column(name = "pay_load")
    String payLoad;

    public ExternalTransferDepositSuccessResponseOutBox(String payLoad) {
        int value = atomicInteger.getAndIncrement();
        id = Long.valueOf(value);
        this.payLoad = payLoad;
    }
}


