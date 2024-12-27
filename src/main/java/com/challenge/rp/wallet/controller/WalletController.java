package com.challenge.rp.wallet.controller;

import com.challenge.rp.wallet.dto.*;
import com.challenge.rp.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody WalletCreateRequest requestBody) {

        return  walletService.createWallet(requestBody);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<WalletBalanceResponse> retrieveBalance(@PathVariable String id) {

        return ResponseEntity.ok(walletService.getBalance(UUID.fromString(id)));

    }


    @GetMapping("/{id}/historic-balance")
    public ResponseEntity<WalletBalanceResponse> retrieveHistoricalBalance(@PathVariable String id,
                                                                       @RequestBody HistoricalBalanceRequest request){

        return ResponseEntity.ok(walletService.getHistoricalBalance(id, request));

    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<String> withdraw(@RequestHeader(value = "Reference-id") UUID referenceId, @PathVariable String id, @RequestBody WithdrawRequest request) {
        walletService.withdraw(id, request, referenceId);
        return ResponseEntity.ok( "Withdrawal successful");
    }

    @PostMapping("{id}/deposit")
    public ResponseEntity<String> deposit(@RequestHeader(value = "Reference-id") UUID referenceId, @PathVariable String id, @RequestBody DepositRequest request) {

        walletService.deposit(id, request, referenceId);
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/transfer")

    public ResponseEntity<String> transferFunds(@RequestHeader(value = "Reference-id") UUID referenceId,@RequestBody TransferRequest request) {

        walletService.transfer(request, referenceId);
        return ResponseEntity.ok("Transfer successful");
    }
}
