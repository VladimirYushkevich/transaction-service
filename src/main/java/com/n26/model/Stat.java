package com.n26.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Stat {
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal sum = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal avg = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal max = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigDecimal min = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
    private Integer count = 0;

    public BigDecimal getAvg() {
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String toString() {
        return "Stat{" +
                "sum=" + sum +
                ", avg=" + getAvg() +
                ", max=" + max +
                ", min=" + min +
                ", count=" + count +
                '}';
    }
}
