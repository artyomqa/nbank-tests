package models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeNameRequest extends BaseModel {
    private String name;
}
