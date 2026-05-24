package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, url.getCreatedAt());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
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

            preparedStatement.setLong(1, id);

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

    public static List<Url> getEntities() throws SQLException {
        String sql = "SELECT * FROM urls ORDER BY created_at DESC";

        try (var conn = dataSource.getConnection();
            var preparedStatement = conn.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();

            List<Url> urls = new ArrayList<>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                Url url = new Url(name, createdAt);
                url.setId(id);
                urls.add(url);
            }
            return urls;
        }
    }

    public static void removeAll() throws SQLException {
        String sql = "DELETE FROM urls ";
        try (var conn = dataSource.getConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
}
