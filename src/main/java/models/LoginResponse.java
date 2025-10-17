package models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LoginResponse extends BaseModel {
    private String username;
    private String role;
}
