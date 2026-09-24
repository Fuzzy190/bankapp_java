package com.ciicc.bankapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequestDto {
    private String accountNumber;
    private String pin;
    private BigDecimal amount;
    private String destinationAccountNumber; // Only used for transfers
}