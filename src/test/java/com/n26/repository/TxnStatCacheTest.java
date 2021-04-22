package com.n26.repository;

import com.n26.config.TxnStatCacheConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TxnStatCacheTest {
    @Mock
    private TxnStatCacheConfig txnStatCacheConfig;
    private TxnStatCache txnStatCache;

    @Before
    public void setUp() {
        when(txnStatCacheConfig.getWindow()).thenReturn(5000); // 5 buckets
        when(txnStatCacheConfig.getStep()).thenReturn(1000); // with 1000 ms step
    }

    @Test
    public void should_read_from_cache_after_expiration() {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(100)); // bucket index 0
        txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(2500)); // bucket index 2

        assertEquals(Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
                txnStatCache.read(now.plusMillis(3900)).collect(Collectors.toList())); // has everything
        assertEquals(Collections.singletonList(BigDecimal.valueOf(2)),
                txnStatCache.read(now.plusMillis(6900)).collect(Collectors.toList())); // bucket index at 0 expired
        assertEquals(Collections.emptyList(),
                txnStatCache.read(now.plusMillis(7400)).collect(Collectors.toList())); // bucket index at 1 expired
    }

    @Test
    public void should_write_without_oder_and_read_from_cache_after_expiration() {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(3500)); // bucket index 0
        txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(1500)); // out of order, bucket index 0

        assertEquals(Arrays.asList(BigDecimal.valueOf(2), BigDecimal.valueOf(1)),
                txnStatCache.read(now.plusMillis(3600)).collect(Collectors.toList())); // has 2 amounts
        assertEquals(Collections.singletonList(BigDecimal.valueOf(1)),
                txnStatCache.read(now.plusMillis(6600)).collect(Collectors.toList())); // first expired
    }

    @Test
    public void should_write_in_cache_after_expiration() {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(500)); // bucket index 0
        txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(5900)); // replace previous

        assertEquals(Collections.singletonList(BigDecimal.valueOf(2)),
                txnStatCache.read(now.plusMillis(5500)).collect(Collectors.toList())); // has only last
    }

    @Test
    public void should_write_in_cache_after_very_long_expiration() {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(500)); // bucket index 0
        txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(21900)); // new buckets after multi window expiration

        assertEquals(Collections.singletonList(BigDecimal.valueOf(2)),
                txnStatCache.read(now.plusMillis(23500)).collect(Collectors.toList())); // has only last
    }

    @Test
    public void should_write_in_cache_with_collisions() {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(500)); // bucket index 0
        txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(700)); // bucket index 0

        assertEquals(Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
                txnStatCache.read(now.plusMillis(1800)).collect(Collectors.toList())); // has 2 values
    }

    @Test
    public void should_write_in_cache_concurrently() throws Exception {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        Thread firstThread = new Thread(() -> {
            txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(500)); // bucket index 0
            txnStatCache.write(BigDecimal.valueOf(3), now.plusMillis(1500)); // bucket index 1
        });
        Thread secondThread = new Thread(() -> {
            txnStatCache.write(BigDecimal.valueOf(2), now.plusMillis(700)); // bucket index 0
        });

        firstThread.start();
        secondThread.start();

        firstThread.join();
        secondThread.join();
        assertEquals(Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
                txnStatCache.read(now.plusMillis(1800)).collect(Collectors.toList())); // has 3  values, order is due to java volatile behaviour for buckets
    }

    @Test
    public void should_write_in_cache_and_delete_concurrently() throws Exception {
        Instant now = Instant.now();

        txnStatCache = new TxnStatCache(txnStatCacheConfig);

        Thread firstThread = new Thread(() -> {
            txnStatCache.write(BigDecimal.valueOf(1), now.plusMillis(500));
            txnStatCache.write(BigDecimal.valueOf(3), now.plusMillis(1500));
        });
        Thread secondThread = new Thread(() -> {
            txnStatCache.deleteAll();
        });

        firstThread.start();
        firstThread.join();

        secondThread.start();
        secondThread.join();
        assertEquals(Collections.emptyList(),
                txnStatCache.read(now.plusMillis(1800)).collect(Collectors.toList()));
    }
}
