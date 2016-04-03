package company.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class Transaction {
    private Long id;
    private Double amount;
    private String type;
    private Long parentId;
    private Sum sum;
    private Double topSum;

    @Tolerate
    public Transaction() {
    }
}
