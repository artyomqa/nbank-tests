package models;

import lombok.Data;

import java.util.List;

@Data
public class DepositMoneyResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions;
}
