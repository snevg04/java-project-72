package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.dto.BasePage;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.rendering.template.JavalinJte;
import gg.jte.resolve.ResourceCodeResolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static io.javalin.rendering.template.TemplateUtil.model;

public class App {
    public static void main(String[] args) {
        var app = getApp();
        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static Javalin getApp() {
        var hikariConfig = new HikariConfig();
        var jdbcUrl = System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
        System.out.println(jdbcUrl);

        hikariConfig.setJdbcUrl(jdbcUrl);


        var dataSource = new HikariDataSource(hikariConfig);

        var url = App.class.getClassLoader().getResourceAsStream("schema.sql");
        var sql = new BufferedReader(new InputStreamReader(url))
                .lines().collect(Collectors.joining("\n"));

        BaseRepository.dataSource = dataSource;



        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
            config.routes.get("/", ctx -> {
                var page = new BasePage();
                ctx.render("index.jte", model("page", page));
            });
        });

        return app;
    }
}
