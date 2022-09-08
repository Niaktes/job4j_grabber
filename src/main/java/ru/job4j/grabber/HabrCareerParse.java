package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;
    public static final int PAGES = 5;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }


    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGES; i++) {
            try {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> posts.add(createNewPost(row)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Program execution was interrupted!");
            }
        }
        return posts;
    }


    private String retrieveDescription(String link) {
        String description = "";
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element rows = document.select(".collapsible-description__content").first();
            description = rows.wholeText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }

    private Post createNewPost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        String postTitle = titleElement.text();
        Element linkElement = titleElement.child(0);
        String postLink = linkElement.attr("abs:href");
        String postDescription = retrieveDescription(postLink);
        Element dateElement = row.select(".vacancy-card__date").first();
        LocalDateTime postCreatedDate = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
        return new Post(postTitle, postLink, postDescription, postCreatedDate);
    }


    public static void main(String[] args) {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateTimeParser);
        String link = "http://career.habr.com/vacancies/java_developer?page=";
        List<Post> posts = parser.list(link);
    }
}