package api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transaction extends BaseModel {
    private int id;
    private float amount;
    private String type;
    private String timestamp;
    private RelatedAccount relatedAccount;
    private String status;
    private boolean fraudCheckRequired;
    private String timestampAsString;
    private int relatedAccountId;
    private float amountAsDouble;
}
