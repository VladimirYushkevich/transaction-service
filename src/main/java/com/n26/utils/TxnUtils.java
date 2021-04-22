package com.n26.utils;

import com.n26.model.Stat;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.stream.Collector;

@UtilityClass
public class TxnUtils {

    public static Collector<BigDecimal, Stat, Stat> combineTxnStat() {
        return Collector.of(
                Stat::new,
                (result, amount) -> {
                    result.setSum(result.getSum().add(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    result.setCount(result.getCount() + 1);
                    if (result.getMin().equals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP))) {
                        result.setMin(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        result.setMin(amount.min(result.getMin()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    result.setMax(amount.max(result.getMax()).setScale(2, BigDecimal.ROUND_HALF_UP));
                },
                (left, right) -> left
        );
    }
}
