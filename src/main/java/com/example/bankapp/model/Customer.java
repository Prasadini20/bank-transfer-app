package com.example.bankapp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name = "account_number", length = 6)
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{6}",
             message = "Account number must contain 6 digits")
    private String accountNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Column(unique = true)
    private String email;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00",
                message = "Balance cannot be negative")
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    private BigDecimal balance;
}
