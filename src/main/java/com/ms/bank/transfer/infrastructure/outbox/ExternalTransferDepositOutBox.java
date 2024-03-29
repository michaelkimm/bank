package com.ms.bank.transfer.infrastructure.outbox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@TableGenerator(
        name = "EXTERNAL_TRANSFER_DEPOSIT_OUTBOX_SEQ_GENERATOR",
        table = "CUSTOM_SEQUENCE"
        , pkColumnValue = "EXTERNAL_TRANSFER_DEPOSIT_OUTBOX_SEQ"
        , allocationSize = 1
)
@NoArgsConstructor
@Getter
@Entity
public class ExternalTransferDepositOutBox {

    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "EXTERNAL_TRANSFER_DEPOSIT_OUTBOX_SEQ_GENERATOR")
    Long id;

    @Lob
    @Column(name = "pay_load")
    String payLoad;

    public ExternalTransferDepositOutBox(String payLoad) {
        this.payLoad = payLoad;
    }
}
