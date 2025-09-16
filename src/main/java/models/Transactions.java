package models;

import lombok.Data;

@Data
public class Transactions extends BaseModel {
    private int id;
    private float amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}
