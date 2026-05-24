package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {

    public static void index(Context ctx) {
        String flash = ctx.consumeSessionAttribute("flash");
        var page = new BasePage();
        page.setFlash(flash);
        ctx.render("index.jte", model("page", page));
    }
    public static void create(Context ctx) throws SQLException {
        try {
            var input = ctx.formParam("url");

            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException();
            }

            var uri = new URI(input);
            var url = uri.toURL();

            var protocol = url.getProtocol();
            var host = url.getHost();
            var port = url.getPort();
            String normalizedURL;

            if (port != -1) {
                normalizedURL = protocol + "://" + host + ":" + port;
            } else if (host != null && host.contains(".")) {
                normalizedURL = protocol + "://" + host;
            } else {
                throw new IllegalArgumentException();
            }

            var found = UrlRepository.findByName(normalizedURL);

            if (found.isEmpty()) {
                var entry = new Url(normalizedURL, Timestamp.from(Instant.now()));
                UrlRepository.save(entry);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.redirect("/urls/" + entry.getId());
            } else {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.redirect("/urls/" + found.get().getId());
            }

        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            var page = new BasePage();
            page.setFlash("Некорректный URL");
            ctx.status(422).render("index.jte", model("page", page));
        }
    }

    public static void list(Context ctx) throws SQLException {
        List<Url> urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));
        String flash = ctx.consumeSessionAttribute("flash");
        var page = new UrlPage(url);
        page.setFlash(flash);
        ctx.render("urls/show.jte", model("page", page));
    }
}
