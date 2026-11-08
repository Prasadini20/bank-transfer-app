package com.example.bankapp.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;

@Repository
public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    // 1. Latest transactions
    @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC")
    List<BankTransaction> findAllLatest();

    // 2. Account transaction history
    @Query("SELECT t FROM BankTransaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    List<BankTransaction> findHistory(
            @Param("account") String account);

    // 3. Find transactions by status
    @Query("SELECT t FROM BankTransaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<BankTransaction> findByStatusLatest(
            @Param("status") TransactionStatus status);

    // 4. Count transactions by status
    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
    long countByStatus(
            @Param("status") TransactionStatus status);

    // 5. Large successful transfers
    @Query("SELECT t FROM BankTransaction t WHERE t.status = 'SUCCESS' AND t.description LIKE 'TRANSFER%' AND t.amount > :min ORDER BY t.createdAt DESC")
    List<BankTransaction> findLargeTransfers(
            @Param("min") BigDecimal min);

    // 6. Total amount sent (without fees)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM BankTransaction t WHERE t.fromAccount = :account AND t.status = 'SUCCESS' AND t.description LIKE 'TRANSFER%'")
    BigDecimal getTotalSent(
            @Param("account") String account);
}
