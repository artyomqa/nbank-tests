package models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class TransferMoneyRequest extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
}
