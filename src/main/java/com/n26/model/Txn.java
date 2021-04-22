package com.n26.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Txn {
    private BigDecimal amount;
    private Instant timestamp;
}
