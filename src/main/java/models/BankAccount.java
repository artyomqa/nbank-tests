package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseModel {
    private int id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions;
}
