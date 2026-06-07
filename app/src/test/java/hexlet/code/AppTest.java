package hexlet.code;

import hexlet.code.controller.UrlController;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class AppTest {
    private Javalin app;
    private static MockWebServer mockServer;
    private static String baseUrl;


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
            assertEquals(200, response.code());
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
            assertEquals(200, response.code());
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
            assertEquals(200, response2.code());
            assertTrue(body.contains("data-test=\"url\""));
        });
    }

    @Test
    public void getUrlCheckTest() throws Exception {

        mockServer = new MockWebServer();

        mockServer.enqueue(new MockResponse.Builder()
                .status("HTTP/1.1 200 OK")
                .body("""
                <html>
                    <head>
                        <title>Test Title</title>
                        <meta name="description" content="Test description">
                    </head>
                    <body>
                        <h1>Test H1</h1>
                    </body>
                </html>
            """)
                .build());

        mockServer.start();

        baseUrl = mockServer.url("/").toString()
                .replaceAll("/$", "");

        JavalinTest.test(app, (server, client) -> {

            var createResponse = client.post("/urls", "url=" + baseUrl);
            assertEquals(200, createResponse.code());

            var savedUrl = UrlRepository.findByName(baseUrl);
            assertTrue(savedUrl.isPresent());

            var urlId = savedUrl.get().getId();

            var response = client.post("/urls/" + urlId + "/checks");
            assertEquals(200, response.code());

            var checks = CheckRepository.findByUrlId(urlId);

            assertFalse(checks.isEmpty());

            var check = checks.get(0);

            assertEquals(200, check.getStatusCode());
            assertEquals("Test Title", check.getTitle());
            assertEquals("Test H1", check.getH1());
            assertEquals("Test description", check.getDescription());
        });

        mockServer.close();
    }

    @Test
    public void getUrlCheckFailTest() throws Exception {

        mockServer = new MockWebServer();

        mockServer.enqueue(new MockResponse.Builder()
                .status("HTTP/1.1 404 Not Found")
                .body("Not found")
                .build());

        mockServer.start();

        baseUrl = mockServer.url("/").toString()
                .replaceAll("/$", "");

        JavalinTest.test(app, (server, client) -> {

            client.post("/urls", "url=" + baseUrl);

            var savedUrl = UrlRepository.findByName(baseUrl);

            assertTrue(savedUrl.isPresent());

            var urlId = savedUrl.get().getId();

            client.post("/urls/" + urlId + "/checks");

            var checks = CheckRepository.findByUrlId(urlId);

            assertTrue(checks.isEmpty());
        });

        mockServer.close();
    }

    @Test
    public void urlCheckTrimmingTest() throws Exception {
        String shortText = "hello";
        String longText = "a".repeat(300);
        String result = UrlController.trim(longText);

        assertEquals("hello", UrlController.trim(shortText));
        assertNull(UrlController.trim(null));
        assertEquals(203, result.length()); // 200 + "..."
        assertTrue(result.endsWith("..."));
        assertTrue(result.startsWith("a"));
    }
}
