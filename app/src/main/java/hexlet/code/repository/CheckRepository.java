package hexlet.code.repository;


import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CheckRepository extends BaseRepository {
    public static void save(UrlCheck check) throws SQLException {
        String sql = "INSERT INTO url_checks (url_id, status_code, h1,"
                + " title, description, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, check.getUrlId());
            preparedStatement.setInt(2, check.getStatusCode());
            preparedStatement.setString(3, check.getTitle());
            preparedStatement.setString(4, check.getH1());
            preparedStatement.setString(5, check.getDescription());
            preparedStatement.setTimestamp(6, check.getCreatedAt());
            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                check.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB has not returned an id after saving an entity");
            }
        }
    }

    public static List<UrlCheck> findByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, urlId);

            var checks = new ArrayList<UrlCheck>();

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var foundId = resultSet.getLong("id");
                var foundCode = resultSet.getInt("status_code");
                var foundH1 = resultSet.getString("h1");
                var foundTitle = resultSet.getString("title");
                var foundDescription = resultSet.getString("description");
                var foundCreatedAt = resultSet.getTimestamp("created_at");

                var check = new UrlCheck(urlId, foundCode, foundTitle,
                        foundH1, foundDescription, foundCreatedAt
                );

                check.setId(foundId);
                checks.add(check);
            }
            return checks;
        }
    }
}
