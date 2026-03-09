package api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RelatedAccount extends BaseModel {
    private int id;
    private String accountNumber;
    private float balance;
}
