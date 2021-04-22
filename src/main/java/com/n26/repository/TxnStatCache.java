package com.n26.repository;

import com.n26.config.TxnStatCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class TxnStatCache {
    private final TxnStatCacheConfig txnStatCacheConfig;
    private volatile LinkedList<?>[] buckets; // for collisions
    private volatile Long lastWriteTime;

    public TxnStatCache(TxnStatCacheConfig txnStatCacheConfig) {
        this.txnStatCacheConfig = txnStatCacheConfig;
        reset();
        log.debug("Initialized cache with {} config. Total buckets: {}.", txnStatCacheConfig, buckets.length);
    }

    private void reset() {
        this.buckets = new LinkedList<?>[txnStatCacheConfig.getWindow() / txnStatCacheConfig.getStep()];
        this.lastWriteTime = 0L;
    }

    @SuppressWarnings("unchecked")
    synchronized public void write(BigDecimal value, Instant instant) {
        long txnTime = instant.toEpochMilli();
        if (lastWriteTime == 0) {
            lastWriteTime = txnTime;
        }
        int bucketIndex = getBucketIndex(txnTime); // can overflow array boundary
        int shiftedIndex = getShiftedIndex(bucketIndex); // index after shift
        if (bucketIndex != shiftedIndex) {
            lastWriteTime = txnTime;
        }

        buckets = getLatestCopy(bucketIndex); // copy buckets

        log.debug("Writing into cache, bucket index: {}, shiftedIndex: {}", bucketIndex, shiftedIndex);
        LinkedList<BigDecimal> bucketValues = Optional.ofNullable((LinkedList<BigDecimal>) buckets[shiftedIndex])
                .orElse(new LinkedList<>());
        bucketValues.add(value);
        buckets[shiftedIndex] = bucketValues;
    }

    @SuppressWarnings("unchecked")
    synchronized public Stream<BigDecimal> read(Instant instant) {
        int bucketIndex = getBucketIndex(instant.toEpochMilli());
        log.debug("Reading from cache, up to bucket index: {}", bucketIndex);

        return Arrays.stream(getLatestCopy(bucketIndex))
                .filter(Objects::nonNull)
                .flatMap(ll -> ((LinkedList<BigDecimal>) ll).stream());
    }

    synchronized public void deleteAll() {
        reset();
    }

    private LinkedList<?>[] getLatestCopy(int bucketIndex) {
        if (lastWriteTime == 0 || Math.abs(bucketIndex) / buckets.length > 1) { // first and too late request
            return new LinkedList<?>[buckets.length];
        }

        if (bucketIndex != getShiftedIndex(bucketIndex)) {
            LinkedList<?>[] copy = new LinkedList<?>[buckets.length];
            int start = bucketIndex > 0 ? bucketIndex % buckets.length + 1 : 0; // +1 to exclude old value
            int end = buckets.length - 1;
            int destPos = bucketIndex > 0 ? 0 : Math.abs(bucketIndex);
            int length = bucketIndex > 0 ? buckets.length - start : buckets.length - destPos;

            log.debug("Copying array from {} to {}, destinationPos {}, length {}", start, end, destPos, length);
            System.arraycopy(buckets, start, copy, destPos, length);

            return copy;
        }

        return buckets.clone();
    }

    private int getShiftedIndex(int bucketIndex) {
        if (bucketIndex >= buckets.length) { // produces left shift for expiration
            return bucketIndex % buckets.length;
        }
        // produces right shift for earliest transactions or the same value as bucketIndex
        return Math.max(bucketIndex, 0);
    }

    private int getBucketIndex(Long time) {
        return (int) (time - lastWriteTime) / txnStatCacheConfig.getStep();
    }
}
