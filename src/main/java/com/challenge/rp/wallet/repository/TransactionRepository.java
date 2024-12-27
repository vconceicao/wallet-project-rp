package com.challenge.rp.wallet.repository;

import com.challenge.rp.wallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :id AND t.createdAt BETWEEN :beginDateTime AND :endDateTime")
    List<Transaction> findAllTransactionsBetween(
            @Param("id") UUID id,
            @Param("beginDateTime") LocalDateTime beginDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

}
