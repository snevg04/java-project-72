package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    public static void create(Context ctx) throws SQLException {
        try {
            var input = ctx.formParam("url");
            var uri = new URI(input);
            var url = uri.toURL();

            var protocol = url.getProtocol();
            var host = url.getHost();
            var port = url.getPort();
            String normalizedURL;

            if (port != -1) {
                normalizedURL = protocol + "://" + host + ":" + port;
            } else {
                normalizedURL = protocol + "://" + host;
            }

            var newEntry = new Url(normalizedURL, Timestamp.from(Instant.now()));
            var newName = newEntry.getName();

            var found = UrlRepository.findByName(newName);

            if (found.isEmpty()) {
                UrlRepository.save(newEntry);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.redirect("/urls/" + newEntry.getId());
            } else {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.redirect("/urls/" + found.get().getId());
            }

        } catch (MalformedURLException | URISyntaxException e) {
            var page = new BasePage();
            page.setFlash("Некорректный URL");
            ctx.status(422).render("index.jte", model("page", page));
        }
    }

    public static void get(Context ctx) {
        //сюда добавить хендлер для get-запроса
        // должен отработать на show по id
        //должен принять и пробросить флешку
    }
}
