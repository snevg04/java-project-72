package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        List<UrlCheck> checks = CheckRepository.findByUrlId(id);
        var page = new UrlPage(url, checks);
        page.setFlash(flash);
        ctx.render("urls/show.jte", model("page", page));
    }

    public static void check(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id).get();

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();

            var statusCode = response.getStatus();

            if (statusCode >= 400) {
                ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                ctx.redirect("/urls/" + id);
                return;
            }

            String html = response.getBody();
            Document doc = Jsoup.parse(html);
            String title = doc.title();
            var h1Element = doc.selectFirst("h1");
            String h1 = h1Element != null ? h1Element.text() : null;
            Element meta = doc.selectFirst("meta[name=description]");
            String description = meta != null ? meta.attr("content") : null;

            var trimmedTitle = trim(title);
            var trimmedH1 = trim(h1);
            var trimmedDescription = trim(description);

            var check = new UrlCheck(id, statusCode, trimmedTitle,
                    trimmedH1, trimmedDescription, Timestamp.from(Instant.now()));
            CheckRepository.save(check);
            ctx.sessionAttribute("flash", "Страница успешно проверена");

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
        }

        ctx.redirect("/urls/" + id);
    }

    public static String trim(String str) {

        if (str == null) {
            return null;
        }

        if (str.length() > 200) {
            return str.substring(0, 200) + "...";
        }

        return str;
    }
}
