package com.ms.bank.transfer.infrastructure.outbox;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OutBox {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String aggregateId;     // 메세지 브로커에서 중복 이벤트 없애기 위함

    private String aggregateType;   // 변경이 발생한 도메인 이름

    private String eventType;       // 발생한 이벤트

    private String payload;         // 도메인 entity 변경사항(JSON)
    
    private Boolean processed = Boolean.FALSE;      // 처리 유무
}
