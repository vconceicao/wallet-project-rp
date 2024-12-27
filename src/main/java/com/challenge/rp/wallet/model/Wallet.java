package com.challenge.rp.wallet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Convert(converter = UUIDBinaryConverter.class)
    private UUID id;

     @Column(name = "user_id", nullable = false, unique = true)
     @Convert(converter = UUIDBinaryConverter.class)
     private UUID userId;
    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant  createdAt;

    @Version
    private Long version; // Used for optimistic locking

    public Wallet() {
    }

    public Wallet(UUID userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getUserId() {
        return userId;
    }


    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }




}
