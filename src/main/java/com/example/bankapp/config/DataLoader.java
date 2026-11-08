package com.example.bankapp.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final CustomerRepository customerRepository;

    @Bean
    public CommandLineRunner loadData() {

        return args -> {

            if (customerRepository.count() == 0) {

                // Customer 1 - Kamal
                Customer kamal = new Customer();
                kamal.setAccountNumber("100001");
                kamal.setFullName("Kamal Perera");
                kamal.setEmail("kamal@gmail.com");
                kamal.setBalance(new BigDecimal("50000.00"));

                // Customer 2 - Nimali
                Customer nimali = new Customer();
                nimali.setAccountNumber("100002");
                nimali.setFullName("Nimali Silva");
                nimali.setEmail("nimali@gmail.com");
                nimali.setBalance(new BigDecimal("25000.00"));

                // Customer 3 - Saman
                Customer saman = new Customer();
                saman.setAccountNumber("100003");
                saman.setFullName("Saman Fernando");
                saman.setEmail("saman@gmail.com");
                saman.setBalance(new BigDecimal("10000.00"));

                // Bank account for transfer fees
                Customer bank = new Customer();
                bank.setAccountNumber("999999");
                bank.setFullName("Bank Fee Account");
                bank.setEmail("bank@gmail.com");
                bank.setBalance(BigDecimal.ZERO);

                customerRepository.save(kamal);
                customerRepository.save(nimali);
                customerRepository.save(saman);
                customerRepository.save(bank);

                System.out.println("Sample customers loaded successfully!");
            }
        };
    }
}
