package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.ZonedDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "http://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select(".vacancy-card__date").first();
            ZonedDateTime dateTime = ZonedDateTime.parse(dateElement.child(0).attr("datetime"));
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String month = dateTime.getMonth().name();
            int day = dateTime.getDayOfMonth();
            System.out.printf("%s %s dated %s %d%n", vacancyName, link, month, day);
        });
    }

}
