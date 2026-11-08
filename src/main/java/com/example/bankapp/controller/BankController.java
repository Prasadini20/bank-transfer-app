package com.example.bankapp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Dashboard
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("customers",
                bankService.searchCustomers(keyword));

        model.addAttribute("allCustomers",
                bankService.getAllCustomers());

        model.addAttribute("transactions",
                bankService.getLatestTransactions());

        model.addAttribute("totalCustomers",
                bankService.getAllCustomers().size());

        model.addAttribute("totalBalance",
                bankService.getTotalBalance());

        model.addAttribute("successCount",
                bankService.getSuccessCount());

        model.addAttribute("failedCount",
                bankService.getFailedCount());

        model.addAttribute("keyword", keyword);

        // Preserve form objects after validation redirect
        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest",
                    new TransferRequest());
        }

        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer",
                    new Customer());
        }

        return "bank";
    }

    // Add customer
    @PostMapping("/customers")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.customer",
                    result);

            redirectAttributes.addFlashAttribute(
                    "customer", customer);

            return "redirect:/";
        }

        try {
            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Customer added successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // Transfer money
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest")
            TransferRequest transferRequest,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.transferRequest",
                    result);

            redirectAttributes.addFlashAttribute(
                    "transferRequest", transferRequest);

            return "redirect:/";
        }

        try {

            bankService.transfer(transferRequest);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Money transferred successfully!");

        } catch (Exception e) {

            auditService.recordFailure(
                    transferRequest.getFromAccount(),
                    transferRequest.getToAccount(),
                    transferRequest.getAmount(),
                    e.getMessage());

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // Account history
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        Customer customer =
                bankService.getCustomer(accountNumber);

        model.addAttribute("customer", customer);

        model.addAttribute("transactions",
                bankService.getAccountHistory(accountNumber));

        model.addAttribute("totalSent",
                bankService.getTotalSent(accountNumber));

        return "history";
    }

    // Deposit page
    @GetMapping("/deposit")
    public String depositPage(Model model) {

        model.addAttribute("customers",
                bankService.getAllCustomers());

        return "deposit";
    }

    // Deposit money
    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.deposit(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Money deposited successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/deposit";
    }

    // Withdraw page
    @GetMapping("/withdraw")
    public String withdrawPage(Model model) {

        model.addAttribute("customers",
                bankService.getAllCustomers());

        return "withdraw";
    }

    // Withdraw money
    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.withdraw(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Money withdrawn successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/withdraw";
    }

    // Large transfers page
    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000")
            BigDecimal min,
            Model model) {

        if (min.compareTo(BigDecimal.ZERO) < 0) {
            min = BigDecimal.ZERO;
        }

        List<BankTransaction> transfers =
                bankService.getLargeTransfers(min);

        model.addAttribute("transfers", transfers);
        model.addAttribute("min", min);

        return "large-transfers";
    }
}