package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserProfiles extends BaseModel {
    private List<UserProfile> users;

    @JsonCreator
    public UserProfiles(List<UserProfile> users) {
        this.users = users;
    }

    @JsonValue
    public List<UserProfile> getUserProfiles() {
        return users;
    }
}
