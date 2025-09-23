package models;

import lombok.Data;

@Data
public class TransferMoneyResponse extends BaseModel {
    private String message;
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
}
