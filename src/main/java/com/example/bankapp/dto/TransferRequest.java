package com.example.bankapp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Please select a sender account")
    private String fromAccount;

    @NotBlank(message = "Please select a receiver account")
    private String toAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01",
                message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2,
            message = "Invalid amount")
    private BigDecimal amount;

    private boolean simulateFailure;
}
