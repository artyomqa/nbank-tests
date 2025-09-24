package models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transaction extends BaseModel {
    private int id;
    private float amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}
