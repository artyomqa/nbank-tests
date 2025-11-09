package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DepositMoneyResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private float balance;
    private float depositAmount;
    private int transactionId;
}
