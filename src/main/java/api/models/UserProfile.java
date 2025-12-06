package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseModel {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<BankAccount> accounts;
}
