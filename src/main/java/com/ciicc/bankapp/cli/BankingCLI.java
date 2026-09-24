package com.ciicc.bankapp.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ciicc.bankapp.entity.Account;
import com.ciicc.bankapp.entity.Transaction;
import com.ciicc.bankapp.service.BankingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BankingCLI implements CommandLineRunner {

    private final BankingService bankingService;

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Spring Boot Web Server started. You can use Postman on port 8080.");
        
        while (running) {
            System.out.println("\n=== TERMINAL BANKING MENU ===");
            System.out.println("1. Create Account");
            System.out.println("2. Balance Inquiry");
            System.out.println("3. List Accounts");
            System.out.println("4. Deposit");
            System.out.println("5. Withdraw");
            System.out.println("6. Transfer");
            System.out.println("7. Transaction History");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        System.out.print("Enter Account Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter 4-6 Digit PIN: ");
                        String pin = scanner.nextLine();
                        System.out.print("Enter Initial Deposit: ");
                        BigDecimal deposit = new BigDecimal(scanner.nextLine());
                        Account acc = bankingService.createAccount(name, pin, deposit);
                        System.out.println("Success! Account Number: " + acc.getAccountNumber());
                        break;
                    case "2":
                        System.out.print("Enter Account Number: ");
                        String accNum = scanner.nextLine();
                        System.out.print("Enter PIN: ");
                        String balPin = scanner.nextLine();
                        Account balAcc = bankingService.authenticate(accNum, balPin);
                        System.out.println("Current Balance: P" + balAcc.getBalance());
                        break;
                    case "3":
                        List<Account> accounts = bankingService.listAllAccounts();
                        for (Account a : accounts) {
                            System.out.println(a.getAccountNumber() + " | " + a.getAccountName() + " | P" + a.getBalance());
                        }
                        break;
                    case "4":
                        System.out.print("Enter Account Number: ");
                        String depAcc = scanner.nextLine();
                        System.out.print("Enter Deposit Amount: ");
                        BigDecimal depAmount = new BigDecimal(scanner.nextLine());
                        bankingService.deposit(depAcc, depAmount);
                        System.out.println("Deposit successful!");
                        break;
                    case "5":
                        System.out.print("Enter Account Number: ");
                        String withAcc = scanner.nextLine();
                        System.out.print("Enter PIN: ");
                        String withPin = scanner.nextLine();
                        System.out.print("Enter Withdrawal Amount: ");
                        BigDecimal withAmount = new BigDecimal(scanner.nextLine());
                        bankingService.withdraw(withAcc, withPin, withAmount);
                        System.out.println("Withdrawal successful!");
                        break;
                    case "6":
                        System.out.print("Enter Your Account Number: ");
                        String srcAcc = scanner.nextLine();
                        System.out.print("Enter Your PIN: ");
                        String srcPin = scanner.nextLine();
                        System.out.print("Enter Destination Account Number: ");
                        String destAcc = scanner.nextLine();
                        System.out.print("Enter Transfer Amount: ");
                        BigDecimal txAmount = new BigDecimal(scanner.nextLine());
                        bankingService.transfer(srcAcc, srcPin, destAcc, txAmount);
                        System.out.println("Transfer successful!");
                        break;
                    case "7":
                        System.out.print("Enter Account Number: ");
                        String histAcc = scanner.nextLine();
                        System.out.print("Enter PIN: ");
                        String histPin = scanner.nextLine();
                        List<Transaction> history = bankingService.getHistory(histAcc, histPin);
                        for (Transaction tx : history) {
                            System.out.println(tx.getTransactionType() + " | P" + tx.getAmount() + " | Bal: P" + tx.getBalanceAfter());
                        }
                        break;
                    case "0":
                        System.out.println("Exiting terminal. Web server will continue running.");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }
}