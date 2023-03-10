package com.ms.bank.transfer.infrastructure.outbox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class ExternalTransferDepositOutBox {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Lob
    @Column(name = "pay_load")
    String payLoad;

    public ExternalTransferDepositOutBox(String payLoad) {
        this.payLoad = payLoad;
    }
}
