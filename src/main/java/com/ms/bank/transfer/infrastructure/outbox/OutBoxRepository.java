package com.ms.bank.transfer.infrastructure.outbox;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface OutBoxRepository extends JpaRepository<OutBox, Long> {

    String SKIP_LOCKED = "-2";

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutBox e")
    List<OutBox> findOutBoxesByAggregateTypeEqualsAndProcessedIsFalse(String aggregateType);
}
