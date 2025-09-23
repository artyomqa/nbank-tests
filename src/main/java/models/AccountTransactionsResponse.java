package models;

import lombok.Data;

import java.util.List;

@Data
public class AccountTransactionsResponse extends BaseModel {
    private List<Transaction> transactions;
}
