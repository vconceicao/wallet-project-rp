package com.challenge.rp.wallet.service;

import com.challenge.rp.wallet.dto.*;
import com.challenge.rp.wallet.exception.InsufficientBalanceException;
import com.challenge.rp.wallet.exception.WalletNotFoundException;
import com.challenge.rp.wallet.model.Transaction;
import com.challenge.rp.wallet.model.Wallet;
import com.challenge.rp.wallet.repository.TransactionRepository;
import com.challenge.rp.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;



    @Test
    void testCreateWallet_Success() {
        WalletCreateRequest request = new WalletCreateRequest(UUID.randomUUID());
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        String walletId = walletService.createWallet(request);

        assertNotNull(walletId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testGetBalance_Success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletBalanceResponse response = walletService.getBalance(walletId);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100.00), response.balance());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void testGetBalance_WalletNotFound() {
        UUID walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getBalance(walletId));
        verify(walletRepository).findById(walletId);
    }

    @Test
    void testGetHistoricalBalance_Success() {
        UUID walletId = UUID.randomUUID();
        HistoricalBalanceRequest request = new HistoricalBalanceRequest(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        Transaction transaction = new Transaction();
        transaction.setWallet(new Wallet());
        transaction.getWallet().setBalance(BigDecimal.valueOf(50.00));

        when(walletRepository.existsById(walletId)).thenReturn(true);
        when(transactionRepository.findAllTransactionsBetween(walletId, request.beginDateTime(), request.endDateTime()))
                .thenReturn(List.of(transaction));

        WalletBalanceResponse response = walletService.getHistoricalBalance(walletId.toString(), request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50.00), response.balance());
        verify(walletRepository).existsById(walletId);
        verify(transactionRepository).findAllTransactionsBetween(walletId, request.beginDateTime(), request.endDateTime());
    }

    @Test
    void testGetHistoricalBalance_WalletNotFound() {
        UUID walletId = UUID.randomUUID();
        HistoricalBalanceRequest request = new HistoricalBalanceRequest(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        when(walletRepository.existsById(walletId)).thenReturn(false);

        assertThrows(WalletNotFoundException.class, () -> walletService.getHistoricalBalance(walletId.toString(), request));
        verify(walletRepository).existsById(walletId);
    }

    @Test
    void testWithdraw_Success() {
        UUID walletId = UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(50.00));
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.withdraw(walletId.toString(), request, UUID.randomUUID());

        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        UUID walletId = UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(150.00));
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () -> walletService.withdraw(walletId.toString(), request, UUID.randomUUID()));
        verify(walletRepository).findById(walletId);
    }

    @Test
    void testDeposit_Success() {
        UUID walletId = UUID.randomUUID();
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(50.00));
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.deposit(walletId.toString(), request, UUID.randomUUID());

        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testTransfer_Success() {
        UUID sourceWalletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(sourceWalletId, targetWalletId, BigDecimal.valueOf(50.00));

        Wallet sourceWallet = new Wallet();
        sourceWallet.setBalance(BigDecimal.valueOf(100.00));
        Wallet targetWallet = new Wallet();
        targetWallet.setBalance(BigDecimal.valueOf(50.00));

        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(targetWalletId)).thenReturn(Optional.of(targetWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(sourceWallet).thenReturn(targetWallet);

        walletService.transfer(request, UUID.randomUUID());

        verify(walletRepository).findById(sourceWalletId);
        verify(walletRepository).findById(targetWalletId);
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
