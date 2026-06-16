package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.CheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
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

        var inputUrl = ctx.formParam("url");

        if (inputUrl == null || inputUrl.isBlank()) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        URI parsedUrl;

        try {
            parsedUrl = new URI(inputUrl);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        var protocol = parsedUrl.getScheme();
        var host = parsedUrl.getHost();
        var port = parsedUrl.getPort();

        if (protocol == null
                || (!protocol.equals("http") && !protocol.equals("https"))
                || host == null) {

            var page = new BasePage();
            page.setFlash("Некорректный URL");

            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT)
                    .render("index.jte", model("page", page));
            return;
        }

        String normalizedURL;

        if (port != -1) {
            normalizedURL = protocol + "://" + host + ":" + port;
        } else {
            normalizedURL = protocol + "://" + host;
        }

        var existingUrl = UrlRepository.findByName(normalizedURL);

        if (existingUrl.isPresent()) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/urls/" + existingUrl.orElseThrow().getId());
            return;
        }

        var entry = new Url(normalizedURL);
        entry.setCreatedAt(LocalDateTime.now());
        UrlRepository.save(entry);

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls/" + entry.getId());
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

        try {
            var id = ctx.pathParamAsClass("id", Long.class).get();
            Url url = UrlRepository.find(id)
                    .orElseThrow(Exception::new);

            HttpResponse<String> response = Unirest.get(url.getName()).asString();

            var statusCode = response.getStatus();

            if (statusCode >= HttpStatus.BAD_REQUEST.getCode()) {
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
                    trimmedH1, trimmedDescription);
            check.setCreatedAt(LocalDateTime.now());
            CheckRepository.save(check);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.redirect("/urls/" + id);

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
            var id = ctx.pathParam("id");
            ctx.redirect("/urls/" + id);
        }
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
