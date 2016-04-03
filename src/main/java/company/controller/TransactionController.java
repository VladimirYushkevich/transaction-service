package company.controller;

import company.dto.SumDTO;
import company.dto.TransactionDTO;
import company.service.TransactionService;
import company.validation.TransactionDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactionservice")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionDTOValidator transactionDTOValidator;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(transactionDTOValidator);
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDTO createTransaction(@PathVariable("transaction_id") Long transactionId,
                                            @Validated @RequestBody TransactionDTO payload) {
        return transactionService.saveTransaction(transactionId, payload);
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = RequestMethod.GET)
    public TransactionDTO getTransactionById(@PathVariable("transaction_id") Long transactionId) {
        return transactionService.getById(transactionId);
    }

    @RequestMapping(value = "/sum/{transaction_id}", method = RequestMethod.GET)
    public SumDTO getSumByTransactionId(@PathVariable("transaction_id") Long transactionId) {
        return transactionService.getSumByTransactionId(transactionId);
    }

    @RequestMapping(value = "/types/{type}", method = RequestMethod.GET)
    public List<Long> getIdsByType(@PathVariable String type) {
        return transactionService.getTransactionIdsByType(type);
    }
}
