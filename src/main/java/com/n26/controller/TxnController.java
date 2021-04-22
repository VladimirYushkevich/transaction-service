package com.n26.controller;

import com.n26.model.Stat;
import com.n26.model.Txn;
import com.n26.service.TxnService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TxnController {
    private final TxnService txnService;

    @PostMapping("/transactions")
    public ResponseEntity<String> create(@RequestBody Txn txn) {
        return txnService.create(txn).map(t -> ResponseEntity.status(HttpStatus.CREATED))
                .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT)).build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Stat> getStat() {
        return ResponseEntity.status(HttpStatus.OK).body(txnService.getTxnStat(Instant.now()));
    }

    @DeleteMapping("/transactions")
    public ResponseEntity<String> delete() {
        txnService.deleteAllTxns();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
