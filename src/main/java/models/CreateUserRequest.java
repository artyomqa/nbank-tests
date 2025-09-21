package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.annotations.GeneratePattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest extends BaseModel {
    @GeneratePattern(regex = "[a-zA-Z0-9._-]{3,15}")
    private String username;
    @GeneratePattern(regex = "[A-Z][0-9][!@#$%^&=+][a-z]{5,27}")
    private String password;
    @GeneratePattern(regex = "USER")
    private String role;
}
