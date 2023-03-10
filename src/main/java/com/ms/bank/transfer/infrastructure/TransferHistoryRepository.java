package com.ms.bank.transfer.infrastructure;


import com.ms.bank.transfer.domain.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, TransferHistory.DateAndGUID> {

    Optional<TransferHistory> findTransferHistoryByPublicTransferId(String publicTransferId);
}
