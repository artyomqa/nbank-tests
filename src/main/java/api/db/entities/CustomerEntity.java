package api.db.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class CustomerEntity {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CustomerEntity(ResultSet result) throws SQLException {
        this.id = result.getLong("id");
        this.username = result.getString("username");
        this.password = result.getString("password");
        this.name = result.getString("name");
        this.role = result.getString("role");
        this.createdAt = result.getTimestamp("created_at");
        this.updatedAt = result.getTimestamp("updated_at");
    }
}
