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
public class BankAccounts extends BaseModel {
    private List<BankAccount> accounts;

    @JsonCreator
    public BankAccounts(List<BankAccount> accounts) {
        this.accounts = accounts;
    }

    @JsonValue
    public List<BankAccount> getAccounts() {
        return accounts;
    }
}
