package api.models;

import api.utils.annotations.GeneratePattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNameRequest extends BaseModel {
    @GeneratePattern(regex = "[A-Za-z]{1,20} [A-Za-z]{1,20}")
    private String name;
}
