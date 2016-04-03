package company.utils;

import company.domain.Transaction;
import company.dto.TransactionDTO;

import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * Utility class for mapping transaction to related DTOs.
 */
public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static Transaction buildTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();

        copyProperties(transactionDTO, transaction);

        return transaction;
    }

    public static TransactionDTO buildTransactionDTO(Transaction transaction) {
        TransactionDTO transactionDTO = new TransactionDTO();

        copyProperties(transaction, transactionDTO);

        return transactionDTO;
    }
}
