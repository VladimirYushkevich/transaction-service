package company.model;

import com.google.common.collect.Maps;
import company.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Class containing the logic to store transactions in memory.
 */
@Component
public class TransactionDataSource {

    private final Map<Long, Transaction> transactions;
    private final Map<Long, Double> sums;
    private final Map<String, List<Long>> types;

    public TransactionDataSource() {
        this.transactions = Maps.newHashMap();
        this.sums = Maps.newHashMap();
        this.types = Maps.newHashMap();
    }

    public Transaction persistTransaction(Transaction tx) {
        transactions.put(tx.getId(), tx);

        updateSums(tx);
        updateTypes(tx);

        return tx;
    }

    public Transaction getById(Long id) {
        return transactions.get(id);
    }

    public List<Long> getIdsByType(String type) {
        return types.get(type);
    }

    public Double getSumByTransactionId(Long transactionId){
        return sums.get(transactionId);
    }

    private void updateSums(Transaction tx) {
        Long transactionId = tx.getId();
        Double amount = tx.getAmount();
        Long parentId = tx.getParentId();

        putInSum(transactionId, amount);
        while (nonNull(parentId)) {
            putInSum(parentId, amount);
            parentId = transactions.containsKey(parentId) ? transactions.get(parentId).getParentId() : null;
        }
    }

    private void putInSum(Long transactionId, Double amount) {
        if (sums.containsKey(transactionId)) {
            sums.put(transactionId, sums.get(transactionId) + amount);
        } else {
            sums.put(transactionId, amount);
        }
    }

    private void updateTypes(Transaction tx) {
        String type = tx.getType();
        List<Long> ids = types.get(type);

        if (isNull(ids)) {
            ids = new LinkedList<>();
            ids.add(tx.getId());
            types.put(type, ids);
        } else {
            ids.add(tx.getId());
        }
    }
}
