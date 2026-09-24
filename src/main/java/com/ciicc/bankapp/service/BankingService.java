package com.ciicc.bankapp.service;

import com.ciicc.bankapp.entity.Account;
import com.ciicc.bankapp.entity.Transaction;
import com.ciicc.bankapp.repository.AccountRepository;
import com.ciicc.bankapp.repository.TransactionRepository;
import com.ciicc.bankapp.util.BankUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Account createAccount(String name, String pin, BigDecimal initialDeposit) {
        Account account = new Account();
        account.setAccountNumber(BankUtils.generateAccountNumber());
        account.setAccountName(name);
        account.setPinHash(BankUtils.hashPin(pin));
        account.setBalance(initialDeposit);

        account = accountRepository.save(account);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(account.getAccountNumber(), "DEPOSIT", initialDeposit, initialDeposit, null);
        }
        return account;
    }

    public Account authenticate(String accountNumber, String pin) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found."));
        
        if (!account.getPinHash().equals(BankUtils.hashPin(pin))) {
            throw new RuntimeException("Invalid PIN.");
        }
        return account;
    }

    public List<Account> listAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive.");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        account.setBalance(account.getBalance().add(amount));
        account = accountRepository.save(account);

        recordTransaction(accountNumber, "DEPOSIT", amount, account.getBalance(), null);
        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, String pin, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive.");
        }
        Account account = authenticate(accountNumber, pin);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account = accountRepository.save(account);

        recordTransaction(accountNumber, "WITHDRAW", amount, account.getBalance(), null);
        return account;
    }

    @Transactional
    public void transfer(String sourceAcc, String pin, String destAcc, BigDecimal amount) {
        if (sourceAcc.equals(destAcc)) {
            throw new RuntimeException("Cannot transfer to the same account.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive.");
        }

        Account source = authenticate(sourceAcc, pin);
        Account dest = accountRepository.findByAccountNumber(destAcc)
                .orElseThrow(() -> new RuntimeException("Destination account not found."));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance.");
        }

        source.setBalance(source.getBalance().subtract(amount));
        dest.setBalance(dest.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(dest);

        recordTransaction(sourceAcc, "TRANSFER_OUT", amount, source.getBalance(), destAcc);
        recordTransaction(destAcc, "TRANSFER_IN", amount, dest.getBalance(), sourceAcc);
    }

    public List<Transaction> getHistory(String accountNumber, String pin) {
        authenticate(accountNumber, pin);
        return transactionRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    private void recordTransaction(String accNum, String type, BigDecimal amount, BigDecimal balAfter, String refAcc) {
        Transaction tx = new Transaction();
        tx.setTransactionReference(BankUtils.generateTransactionRef());
        tx.setAccountNumber(accNum);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balAfter);
        tx.setReferenceAccount(refAcc);
        transactionRepository.save(tx);
    }
}