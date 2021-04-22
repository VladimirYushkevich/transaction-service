package com.n26;

import com.n26.model.Stat;
import com.n26.utils.TxnUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TxnUtilsTest {

    @Test
    public void should_combine_transactions() {
        Stat stat = Stream.of(BigDecimal.valueOf(2.25d), BigDecimal.valueOf(3.33d), BigDecimal.valueOf(1.56d), BigDecimal.valueOf(4.44d))
                .collect(TxnUtils.combineTxnStat());

        assertEquals(Stat.builder()
                .sum(BigDecimal.valueOf(11.58d))
                .avg(BigDecimal.valueOf(2.90d))
                .max(BigDecimal.valueOf(4.44d))
                .min(BigDecimal.valueOf(1.56d))
                .count(4)
                .build(), stat);
    }
}
