package hexlet.code.dto;

import hexlet.code.model.Url;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
@Setter
public class UrlPage extends BasePage {
    private Url url;
}
