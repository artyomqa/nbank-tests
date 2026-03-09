package api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TransferFraudCheckResponse extends BaseModel {
    private int transactionId;
    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
    private String status;
    private String message;
    private String fraudReason;
    private float fraudRiskScore;
    private boolean requiresVerification;
    private boolean requiresManualReview;
}
