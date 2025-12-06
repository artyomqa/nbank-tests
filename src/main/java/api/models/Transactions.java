package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Transactions extends BaseModel {
    private List<Transaction> transactions;

    @JsonCreator
    public Transactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @JsonValue
    public List<Transaction> getTransactions() {
        return transactions;
    }
}
