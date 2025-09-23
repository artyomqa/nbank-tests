package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserBankAccounts extends BaseModel {
    private BankAccount[] accounts;

    @JsonCreator
    public UserBankAccounts(BankAccount[] accounts) {
        this.accounts = accounts;
    }

    @JsonValue
    public BankAccount[] getAccounts() {
        return accounts;
    }
}
