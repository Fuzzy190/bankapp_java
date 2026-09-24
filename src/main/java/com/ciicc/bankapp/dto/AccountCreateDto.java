package com.ciicc.bankapp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountCreateDto {
    private String accountName;
    private String pin;
    private BigDecimal initialDeposit;
}