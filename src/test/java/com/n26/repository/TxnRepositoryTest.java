package com.n26.repository;

import com.n26.model.Txn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TxnRepositoryTest {
    @Mock
    private TxnStatCache txnStatCache;
    @InjectMocks
    private TxnRepository txnRepository;

    @Test
    public void should_create_transaction() {
        txnRepository.createTxn(new Txn(BigDecimal.ONE, Instant.now()));

        verify(txnStatCache, times(1)).write(any(), any());
    }

    @Test
    public void should_get_transaction_stream() {
        when(txnStatCache.read(any())).thenReturn(Stream.of(BigDecimal.ONE, BigDecimal.TEN));

        Stream<BigDecimal> txnStream = txnRepository.getTxnStream(Instant.now());

        assertEquals(Arrays.asList(BigDecimal.ONE, BigDecimal.TEN), txnStream.collect(Collectors.toList()));
    }

    @Test
    public void should_delete_all_transactions() {
        txnRepository.deleteAllTxns();

        verify(txnStatCache, times(1)).deleteAll();
    }
}
