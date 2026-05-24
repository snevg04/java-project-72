package hexlet.code;

import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    private Javalin app;

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp("jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
        UrlRepository.removeAll();
    }

    @Test
    public void getRootTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            var body = response.body().string();
            Assertions.assertEquals(200, response.code());
            assertTrue(body.contains("name=\"url\""));
        });
    }

    @Test
    public void postUrlTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls", "url=https://google.com");
            var url = UrlRepository.findByName("https://google.com");
            assertTrue(url.isPresent());
            assertEquals(200, response.code());
        });
    }

    @Test
    public void postRedirectToUrlPageTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls", "url=https://google.com");
            var url = UrlRepository.findByName("https://google.com").get();
            assertEquals(200, response.code());
        });
    }

    @Test
    public void postUrlDuplicateTest() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var response1 = client.post("/urls", "url=https://google.com");
            var response2 = client.post("/urls", "url=https://google.com");
        });

        var found = UrlRepository.findByName("https://google.com");
        assertTrue(found.isPresent());
        assertEquals("https://google.com", found.get().getName());
    }

    @Test
    public void getUrlsTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls");
            var body = response.body().string();
            Assertions.assertEquals(200, response.code());
            assertTrue(body.contains("data-test=\"urls\""));
        });
    }

    @Test
    public void getUrlTest() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var response1 = client.post("/urls", "url=https://google.com");
            var savedUrl = UrlRepository.findByName("https://google.com");
            assertTrue(savedUrl.isPresent());
            var response2 = client.get("/urls/" + savedUrl.get().getId());
            var body = response2.body().string();
            Assertions.assertEquals(200, response2.code());
            assertTrue(body.contains("data-test=\"url\""));
        });
    }
}
