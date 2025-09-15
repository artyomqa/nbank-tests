package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest extends BaseModel {
    private String username;
    private String password;
}
