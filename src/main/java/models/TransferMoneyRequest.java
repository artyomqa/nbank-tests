package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferMoneyRequest extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
}
