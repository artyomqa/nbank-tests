package api.db.entities;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class CustomerEntity {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
