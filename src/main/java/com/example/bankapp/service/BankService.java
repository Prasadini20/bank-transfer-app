package com.example.bankapp.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Get customers by keyword
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllCustomers();
        }
        return customerRepository.searchByName(keyword.trim());
    }

    // Get one customer
    public Customer getCustomer(String accountNumber) {
        return customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));
    }

    // Get all transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get account history
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    // Get total bank balance
    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    // Count successful transfers
    public long getSuccessCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.SUCCESS);
    }

    // Count failed transfers
    public long getFailedCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.FAILED);
    }

    // Get large transfers
    public List<BankTransaction> getLargeTransfers(BigDecimal min) {
        return transactionRepository.findLargeTransfers(min);
    }

    // Get total sent
    public BigDecimal getTotalSent(String accountNumber) {
        BigDecimal total = transactionRepository.getTotalSent(accountNumber);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Add new customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new BankException(
                    "Account number already exists");
        }

        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new BankException(
                    "Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Money transfer
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(TransferRequest request)
            throws Exception {

        String from = request.getFromAccount();
        String to = request.getToAccount();

        BigDecimal amount = request.getAmount();

        // Validate accounts
        if (from.equals(to)) {
            throw new BankException(
                    "Sender and receiver must be different accounts");
        }

        // Get sender and receiver
        getCustomer(from);
        getCustomer(to);

        // Debit sender only by transfer amount
        int debitResult = customerRepository.debit(from, amount);

        if (debitResult == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Simulate failure for rollback testing
        if (request.isSimulateFailure()) {
            throw new RuntimeException(
                    "All changes were rolled back (simulated failure)");
        }

        // Credit receiver with full transfer amount
        int creditResult = customerRepository.credit(to, amount);

        if (creditResult == 0) {
            throw new AccountNotFoundException(
                    "Receiver account not found");
        }

        // Save successful transaction
        BankTransaction transaction = new BankTransaction();

        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(
                "TRANSFER - Money transferred successfully");

        return transactionRepository.save(transaction);
    }

    // Deposit money
    @Transactional
    public BankTransaction deposit(String accountNumber,
                                   BigDecimal amount) {

        validateAmount(amount);

        getCustomer(accountNumber);

        customerRepository.credit(accountNumber, amount);

        BankTransaction transaction = new BankTransaction();
        transaction.setFromAccount("CASH");
        transaction.setToAccount(accountNumber);
        transaction.setAmount(amount);
        
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(
                "DEPOSIT - Money deposited successfully");

        return transactionRepository.save(transaction);
    }

    // Withdraw money
    @Transactional
    public BankTransaction withdraw(String accountNumber,
                                    BigDecimal amount) {

        validateAmount(amount);

        getCustomer(accountNumber);

        int result = customerRepository.debit(accountNumber, amount);

        if (result == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        BankTransaction transaction = new BankTransaction();
        transaction.setFromAccount(accountNumber);
        transaction.setToAccount("CASH");
        transaction.setAmount(amount);
        
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(
                "WITHDRAWAL - Money withdrawn successfully");

        return transactionRepository.save(transaction);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0 ||
                amount.scale() > 2 ||
                amount.precision() - amount.scale() > 12) {
            throw new BankException(
                    "Please enter a valid amount");
        }
    }
}
