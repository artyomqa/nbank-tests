package models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transactions extends BaseModel {
    private List<Transaction> transactions;
}
