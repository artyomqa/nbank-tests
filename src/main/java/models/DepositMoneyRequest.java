package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositMoneyRequest extends BaseModel {
    private int id;
    private float balance;
}
