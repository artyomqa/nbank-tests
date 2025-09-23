package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DepositMoneyRequest extends BaseModel {
    private int id;
    private float balance;
}
