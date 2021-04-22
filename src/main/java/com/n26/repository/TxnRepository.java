package com.n26.repository;

import com.n26.model.Txn;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TxnRepository {
    private final TxnStatCache txnStatCache;

    public void createTxn(Txn txn) {
        txnStatCache.write(txn.getAmount(), txn.getTimestamp());
    }

    public Stream<BigDecimal> getTxnStream(Instant instant) {
        return txnStatCache.read(instant);
    }

    public void deleteAllTxns() {
        txnStatCache.deleteAll();
    }
}
