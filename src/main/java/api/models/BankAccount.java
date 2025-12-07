package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseModel {
    private int id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions;
}
