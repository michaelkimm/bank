package com.ms.bank.transfer.infrastructure.outbox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
@Entity
public class ExternalTransferDepositSuccessResponseOutBox {

//    private static AtomicInteger atomicInteger = new AtomicInteger();

//    @Id
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Lob
    @Column(name = "pay_load")
    String payLoad;

    public ExternalTransferDepositSuccessResponseOutBox(String payLoad) {
//        id = Long.valueOf(atomicInteger.getAndIncrement());
        this.payLoad = payLoad;
    }
}


