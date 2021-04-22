package com.n26.service;

import com.n26.config.TxnStatCacheConfig;
import com.n26.exception.FutureTxnException;
import com.n26.model.Stat;
import com.n26.model.Txn;
import com.n26.repository.TxnRepository;
import com.n26.utils.TxnUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TxnService {
    private final TxnRepository txnRepository;
    private final TxnStatCacheConfig txnStatCacheConfig;

    public Optional<Txn> create(Txn txn) {
        Instant txnInstant = txn.getTimestamp();
        Instant now = Instant.now();

        if (txnInstant.isAfter(now)) {
            throw new FutureTxnException();
        }
        if (txnInstant.isBefore(now.minusMillis(txnStatCacheConfig.getWindow()))) {
            return Optional.empty();
        }

        txnRepository.createTxn(txn);

        return Optional.of(txn);
    }

    public void deleteAllTxns() {
        txnRepository.deleteAllTxns();
    }

    public Stat getTxnStat(Instant instant) {
        return txnRepository.getTxnStream(instant).collect(TxnUtils.combineTxnStat());
    }
}
