package com.ms.bank.transfer.infrastructure.outbox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@TableGenerator(
        name = "EXTERNAL_TRANSFER_OUTBOX_SEQ_GENERATOR",
        table = "CUSTOM_SEQUENCE"
        , pkColumnValue = "EXTERNAL_TRANSFER_OUTBOX_SEQ"
        , allocationSize = 1
)
@NoArgsConstructor
@Getter
@Entity
public class ExternalTransferOutBox {

    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "EXTERNAL_TRANSFER_OUTBOX_SEQ_GENERATOR")
    Long id;

    @Lob
    @Column(name = "pay_load")
    String payLoad;

    public ExternalTransferOutBox(String payLoad) {
        this.payLoad = payLoad;
    }
}


