package com.challenge.rp.wallet.service;

import com.challenge.rp.wallet.dto.*;
import com.challenge.rp.wallet.exception.InsufficientBalanceException;
import com.challenge.rp.wallet.exception.WalletNotFoundException;
import com.challenge.rp.wallet.model.Transaction;
import com.challenge.rp.wallet.model.TransactionType;
import com.challenge.rp.wallet.model.Wallet;
import com.challenge.rp.wallet.repository.TransactionRepository;
import com.challenge.rp.wallet.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public String createWallet(WalletCreateRequest requestBody) {
        log.info("Creating wallet for user with ID: {}", requestBody.userId());

        var walletId = walletRepository.save(requestBody.toModel()).getId().toString();
        log.info("Wallet created with ID: {}", walletId);

        return walletId;

    }

    public WalletBalanceResponse getBalance(UUID walletId) {
        log.info("Fetching balance for wallet with ID: {}", walletId);

       return walletRepository.findById(walletId)
               .map(w -> new WalletBalanceResponse(w.getBalance())).orElseThrow(() -> {
                   log.error("Wallet not found for ID: {}", walletId);
                   return new WalletNotFoundException("Wallet not found for ID " + walletId);
               });
    }

    public WalletBalanceResponse getHistoricalBalance(String id, HistoricalBalanceRequest request) {
        UUID walletId = UUID.fromString(id);

        // Check if the wallet exists
        if (!walletRepository.existsById(walletId)) {
            log.error("Wallet not found for ID: {}", walletId);
            throw new WalletNotFoundException("Wallet not found for ID: " + walletId);
        }

        log.info("Fetching historical balance for wallet {} from {} to {}", id, request.beginDateTime(), request.endDateTime());

        // Retrieve transactions in the specified time range
        List<Transaction> transactions = transactionRepository.findAllTransactionsBetween(walletId, request.beginDateTime(), request.endDateTime());

        // Calculate the historical balance
        BigDecimal historicalBalance = BigDecimal.valueOf(
                transactions.stream().mapToDouble(t -> t.getWallet().getBalance().doubleValue()).sum()
        );

        log.debug("Historical balance for wallet {} is {}", walletId, historicalBalance);

        return new WalletBalanceResponse(historicalBalance);

    }

    @Transactional
    @Retryable(
            value = { OptimisticLockException.class }, // Exception to retry on
            maxAttempts = 3,                          // Number of retry attempts
            backoff = @Backoff(delay = 1000)          // Delay between retries (in ms)
    )
    public void withdraw(String id, WithdrawRequest request, UUID referenceId) {

        log.info("Processing withdrawal of {} from wallet {}", request.amount(), id);

        var walletId = UUID.fromString(id);

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for ID: " + walletId));

        // Validate sufficient balance
        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        // Update the balance
        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        walletRepository.save(wallet);

        log.info("Withdrawal of {} from wallet {} successful", request.amount(), walletId);


        // Record the transaction
        Transaction transaction = new Transaction(wallet, TransactionType.WITHDRAW,  request.amount(), referenceId);
        transactionRepository.save(transaction);

        log.info("Transaction recorded with reference ID: {}", referenceId);



    }

    @Transactional
    @Retryable(
            value = { OptimisticLockException.class }, // Exception to retry on
            maxAttempts = 3,                          // Number of retry attempts
            backoff = @Backoff(delay = 1000)          // Delay between retries (in ms)
    )
    public void deposit(String id, DepositRequest request, UUID referenceId) {
        log.info("Processing deposit of {} to wallet {}", request.amount(), id);

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        var  walletId = UUID.fromString(id);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for ID: " + walletId));

        // Update the balance
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        // Record the transaction
        Transaction transaction = new Transaction(wallet, TransactionType.DEPOSIT,  request.amount(), referenceId);
        transactionRepository.save(transaction);
        log.info("Deposit of {} to wallet {} successful", request.amount(), walletId);


    }

    @Transactional
    @Retryable(
            value = { OptimisticLockException.class }, // Exception to retry on
            maxAttempts = 3,                          // Number of retry attempts
            backoff = @Backoff(delay = 1000)          // Delay between retries (in ms)
    )
    public void transfer(TransferRequest request, UUID referenceId) {
        log.info("Processing transfer of {} from wallet {} to wallet {}", request.amount(), request.sourceWalletId(), request.targetWalletId());

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid transfer amount: {}", request.amount());
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        if (request.sourceWalletId().equals(request.targetWalletId())) {
            log.warn("Source and destination wallets are the same. Wallet ID: {}", request.sourceWalletId());
            throw new IllegalArgumentException("Source and destination wallets cannot be the same");
        }

        Wallet sourceWallet = walletRepository.findById(request.sourceWalletId())
                .orElseThrow(() -> {
                    log.error("Source wallet not found. Wallet ID: {}", request.sourceWalletId());
                    return new WalletNotFoundException("Source wallet not found for ID: " + request.sourceWalletId());
                });

        if (sourceWallet.getBalance().compareTo(request.amount()) < 0) {
            log.error("Insufficient balance in source wallet {}. Requested: {}, Available: {}", request.sourceWalletId(), request.amount(), sourceWallet.getBalance());
            throw new InsufficientBalanceException("Insufficient balance in source wallet");
        }

        Wallet destinationWallet = walletRepository.findById(request.targetWalletId())
                .orElseThrow(() -> {
                    log.error("Destination wallet not found. Wallet ID: {}", request.targetWalletId());
                    return new WalletNotFoundException("Destination wallet not found for ID: " + request.targetWalletId());
                });

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        walletRepository.save(sourceWallet);
        destinationWallet.setBalance(destinationWallet.getBalance().add(request.amount()));
        walletRepository.save(destinationWallet);
        log.info("Transfer of {} from wallet {} to wallet {} successful", request.amount(), request.sourceWalletId(), request.targetWalletId());

        Transaction debitTransaction = new Transaction(sourceWallet, TransactionType.WITHDRAW, request.amount(), referenceId);
        Transaction creditTransaction = new Transaction(destinationWallet, TransactionType.DEPOSIT, request.amount(), referenceId);
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        log.info("Transactions recorded with reference ID: {}", referenceId);

    }
}
