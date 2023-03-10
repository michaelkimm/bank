package com.ms.bank.transfer.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalTransferDepositOutBoxRepository extends JpaRepository<ExternalTransferDepositOutBox, Long> {

//    String SKIP_LOCKED = "-2";
//    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select e from ExternalTransferDepositOutBox e where e.id = :id")
//    Optional<ExternalTransferDepositOutBox> findExternalTransferDepositOutBoxForUpdate(@Param("id") Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM external_transfer_deposit_out_box LIMIT 1 FOR UPDATE SKIP LOCKED ")
    Optional<ExternalTransferDepositOutBox> findOneForUpdate();
}
