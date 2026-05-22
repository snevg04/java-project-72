package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, createdAT) VALUES = (?, ?)";
        try (var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, url.getCreatedAt());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if(generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB has not returned an id after saving an entity");
            }
        }
    }

    public static Optional<Url> find(Long id) throws SQLException {
        String sql = "SELECT * FROM urls WHERE id = ?";

        try (var conn = dataSource.getConnection();
            var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setLong(1,id);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");

                var url = new Url(name, createdAt);
                url.setId(id);

                return Optional.of(url);
            }

            return Optional.empty();
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM urls WHERE name = ?";

        try (var conn = dataSource.getConnection();
            var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var actualName = resultSet.getString("name");
                var id = resultSet.getLong("id");
                var createdAt = resultSet.getTimestamp("created_at");

                var url = new Url(actualName, createdAt);
                url.setId(id);

                return Optional.of(url);
            }

            return Optional.empty();
        }
    }
}
