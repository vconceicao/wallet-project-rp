package com.challenge.rp.wallet.dto;

import com.challenge.rp.wallet.model.Wallet;

import java.util.UUID;

public record WalletCreateRequest(UUID userId) {
    public Wallet toModel() {

        return new Wallet(userId);
    }
}
