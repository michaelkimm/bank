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
public interface ExternalTransferDepositOutBoxRepository extends JpaRepository<ExternalTransferDepositOutBox, Long> {

    String SKIP_LOCKED = "-2";
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExternalTransferDepositOutBox e where e.id = :id")
    Optional<ExternalTransferDepositOutBox> findExternalTransferDepositOutBoxForUpdate(@Param("id") Long id);

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExternalTransferDepositOutBox e")
    List<ExternalTransferDepositOutBox> findAllExternalTransferDepositOutBoxForUpdate();

    @Query(nativeQuery = true, value = "SELECT * FROM external_transfer_deposit_out_box LIMIT 1 FOR UPDATE SKIP LOCKED ")
    Optional<ExternalTransferDepositOutBox> findOneForUpdate();
}
