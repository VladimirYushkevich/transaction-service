package company.service;

import company.domain.Transaction;
import company.dto.SumDTO;
import company.dto.TransactionDTO;
import company.exceptions.NotFoundException;
import company.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static company.utils.TransactionMapper.buildTransaction;
import static company.utils.TransactionMapper.buildTransactionDTO;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionDTO saveTransaction(Long id, TransactionDTO transactionDTO) {
        Optional.ofNullable(transactionDTO.getParentId()).ifPresent(pid -> {
            transactionRepository.findById(pid).orElseThrow(NotFoundException::new);
        });


        Transaction tx = buildTransaction(transactionDTO);
        tx.setId(id);
        Transaction createdTransaction = transactionRepository.save(tx);
        return buildTransactionDTO(createdTransaction);
    }

    public TransactionDTO getById(Long id) {
        Transaction tx = transactionRepository.findById(id).orElseThrow(NotFoundException::new);

        return buildTransactionDTO(tx);
    }

    public SumDTO getSumByTransactionId(Long transactionId) {
        Double sum = transactionRepository.getSumByTransactionId(transactionId).orElseThrow(NotFoundException::new);

        return SumDTO.builder().sum(sum).build();
    }

    public List<Long> getTransactionIdsByType(String type) {
        return Optional.ofNullable(transactionRepository.findIdsByType(type)).orElse(new LinkedList<>());
    }
}
