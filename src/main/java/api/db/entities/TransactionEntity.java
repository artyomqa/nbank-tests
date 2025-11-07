package api.db.entities;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
@Getter
public class TransactionEntity {
    private long id;
    private BigDecimal amount;
    private String type;
    private Timestamp timestamp;
    private long accountId;
    private long relatedAccountId;
    private Timestamp createdAt;
}
