package company.validation;

import company.dto.TransactionDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Set;

@Component
public class TransactionDTOValidator extends AbstractDTOValidator<TransactionDTO> {

    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_TYPE = "type";

    @Override
    protected void validate(TransactionDTO dto, Errors errors, Set<Class<?>> hints) {
        if (errors.getFieldValue(FIELD_AMOUNT) == null) {
            errors.rejectValue(FIELD_AMOUNT, CODE_FIELD_REQUIRED);
        }
        if (errors.getFieldValue(FIELD_TYPE) == null) {
            errors.rejectValue(FIELD_TYPE, CODE_FIELD_REQUIRED);
        }
    }
}
