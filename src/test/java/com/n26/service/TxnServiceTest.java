package com.n26.service;

import com.n26.config.TxnStatCacheConfig;
import com.n26.exception.FutureTxnException;
import com.n26.model.Stat;
import com.n26.model.Txn;
import com.n26.repository.TxnRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TxnServiceTest {
    @Mock
    private TxnStatCacheConfig txnStatCacheConfig;
    @Mock
    private TxnRepository txnRepository;
    @InjectMocks
    private TxnService txnService;

    @Before
    public void setUp() {
        when(txnStatCacheConfig.getWindow()).thenReturn(30000); // 60 s
    }

    @Test
    public void should_create_transaction() {
        Txn txn = new Txn(BigDecimal.ONE, Instant.now().minusMillis(20000));

        Optional<Txn> createdTxn = txnService.create(txn);

        verify(txnRepository, times(1)).createTxn(txn);
        assertEquals(Optional.of(txn), createdTxn);
    }

    @Test
    public void should_delete_all_transaction() {
        txnService.deleteAllTxns();

        verify(txnRepository, times(1)).deleteAllTxns();
    }

    @Test(expected = FutureTxnException.class)
    public void should_throw_exception_if_transaction_in_the_future() {
        txnService.create(new Txn(BigDecimal.ONE, Instant.now().plusMillis(10)));
    }

    @Test
    public void should_return_empty_if_transaction_is_too_old() {
        Optional<Txn> txn = txnService.create(new Txn(BigDecimal.ONE, Instant.now().minusMillis(30010)));

        assertEquals(Optional.empty(), txn);
    }

    @Test
    public void should_calculate_transaction_stat() {
        when(txnRepository.getTxnStream(any())).thenReturn(Stream.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)));

        Stat stat = txnService.getTxnStat(Instant.now());

        assertEquals(Stat.builder()
                .sum(BigDecimal.valueOf(6).setScale(2, BigDecimal.ROUND_HALF_UP))
                .avg(BigDecimal.valueOf(2).setScale(2, BigDecimal.ROUND_HALF_UP))
                .max(BigDecimal.valueOf(3).setScale(2, BigDecimal.ROUND_HALF_UP))
                .min(BigDecimal.valueOf(1).setScale(2, BigDecimal.ROUND_HALF_UP))
                .count(3)
                .build(), stat);
    }
}
