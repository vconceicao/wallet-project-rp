package com.challenge.rp.wallet.dto;

import java.time.LocalDateTime;

public record HistoricalBalanceRequest(LocalDateTime beginDateTime, LocalDateTime endDateTime) {
}
