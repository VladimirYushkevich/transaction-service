package company.repository;

import company.domain.Transaction;
import company.model.TransactionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    @Autowired
    private TransactionDataSource transactionDataSource;

    public Transaction save(Transaction tx) {
        transactionDataSource.persistTransaction(tx);

        return tx;
    }

    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(transactionDataSource.getById(id));
    }

    public List<Long> findIdsByType(String type) {
        return transactionDataSource.getIdsByType(type);
    }

    public Optional<Double> getSumByTransactionId(Long transactionId) {
        return Optional.ofNullable(transactionDataSource.getSumByTransactionId(transactionId));
    }
}
