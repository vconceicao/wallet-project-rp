package com.challenge.rp.wallet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_transaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reference_id", "transaction_type"})
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Convert(converter = UUIDBinaryConverter.class)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    public Transaction() {
    }

    public Transaction(Wallet wallet, TransactionType transactionType, BigDecimal amount) {
        this.wallet = wallet;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public Transaction(Wallet wallet, TransactionType transactionType, BigDecimal amount, UUID referenceId) {
        this.wallet = wallet;
        this.transactionType = transactionType;
        this.amount = amount;
        this.referenceId = referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
