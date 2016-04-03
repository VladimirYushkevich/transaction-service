package company.model;

import com.google.common.collect.Maps;
import company.domain.Sum;
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
    private final Map<String, List<Long>> types;

    public TransactionDataSource() {
        this.transactions = Maps.newHashMap();
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

    public Double getSumByTransactionId(Long transactionId) {
        Transaction tx = transactions.get(transactionId);

        Double sum = null;
        if (nonNull(tx)) {
            sum = tx.getSum().getValue() - tx.getTopSum();
        }

        return sum;
    }

    private void updateSums(Transaction tx) {
        Long parentId = tx.getParentId();

        if (nonNull(parentId)) {
            Transaction parent = transactions.get(parentId);

            Sum parentSum = parent.getSum();
            Double parentValue = parentSum.getValue();
            parentSum.setValue(parentValue + tx.getAmount());
            tx.setSum(parentSum);

            Double topSum = parent.getTopSum() + parent.getAmount();
            tx.setTopSum(topSum);
        } else {
            Sum sum = Sum.builder().value(tx.getAmount()).build();
            tx.setSum(sum);
            tx.setTopSum(0.0);
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
