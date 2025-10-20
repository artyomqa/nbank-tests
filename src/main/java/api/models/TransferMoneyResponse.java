package api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class TransferMoneyResponse extends BaseModel {
    private String message;
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
}
