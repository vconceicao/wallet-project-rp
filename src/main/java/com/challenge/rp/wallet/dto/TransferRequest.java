package com.challenge.rp.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount) {
}
