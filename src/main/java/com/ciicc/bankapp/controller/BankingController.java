package com.ciicc.bankapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ciicc.bankapp.dto.AccountCreateDto;
import com.ciicc.bankapp.dto.TransactionRequestDto;
import com.ciicc.bankapp.entity.Account;
import com.ciicc.bankapp.service.BankingService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*") // Allows the HTML file to communicate with the API
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankingController {

    private final BankingService bankingService;

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody AccountCreateDto dto) {
        try {
            Account account = bankingService.createAccount(dto.getAccountName(), dto.getPin(), dto.getInitialDeposit());
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> listAccounts() {
        return ResponseEntity.ok(bankingService.listAllAccounts());
    }

    @PostMapping("/balance")
    public ResponseEntity<?> checkBalance(@RequestBody TransactionRequestDto dto) {
        try {
            Account account = bankingService.authenticate(dto.getAccountNumber(), dto.getPin());
            return ResponseEntity.ok("Current Balance: P" + account.getBalance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequestDto dto) {
        try {
            Account account = bankingService.deposit(dto.getAccountNumber(), dto.getAmount());
            return ResponseEntity.ok("Deposit successful. New Balance: P" + account.getBalance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequestDto dto) {
        try {
            Account account = bankingService.withdraw(dto.getAccountNumber(), dto.getPin(), dto.getAmount());
            return ResponseEntity.ok("Withdrawal successful. New Balance: P" + account.getBalance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionRequestDto dto) {
        try {
            bankingService.transfer(dto.getAccountNumber(), dto.getPin(), dto.getDestinationAccountNumber(), dto.getAmount());
            return ResponseEntity.ok("Transfer successful.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> getHistory(@RequestBody TransactionRequestDto dto) {
        try {
            return ResponseEntity.ok(bankingService.getHistory(dto.getAccountNumber(), dto.getPin()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}