package company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class TransactionDTO {
    private Double amount;
    private String type;
    @JsonProperty("parent_id")
    private Long parentId;

    @Tolerate
    public TransactionDTO() {
    }
}
