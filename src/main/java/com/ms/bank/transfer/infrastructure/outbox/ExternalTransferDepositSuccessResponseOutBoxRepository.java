package com.ms.bank.transfer.infrastructure.outbox;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalTransferDepositSuccessResponseOutBoxRepository extends JpaRepository<ExternalTransferDepositSuccessResponseOutBox, Long> {

    String SKIP_LOCKED = "-2";

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExternalTransferDepositSuccessResponseOutBox e")
    List<ExternalTransferDepositSuccessResponseOutBox> findAllExternalTransferDepositSuccessResponseOutBoxForUpdate();
}
