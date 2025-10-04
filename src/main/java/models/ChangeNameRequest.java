package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import utils.annotations.GeneratePattern;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNameRequest extends BaseModel {
    @GeneratePattern(regex = "[A-Za-z]{1,20} [A-Za-z]{1,20}")
    private String name;
}
