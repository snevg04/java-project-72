package hexlet.code.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
public class Url {
    private long id;
    private String name;
    private Timestamp createdAt;
}
