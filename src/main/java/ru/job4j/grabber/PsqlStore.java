package ru.job4j.grabber;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("store.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        this.cn = DriverManager.getConnection(
                cfg.getProperty("store.url"),
                cfg.getProperty("store.login"),
                cfg.getProperty("store.password")
        );
    }


    @Override
    public Post save(Post post) {
        try (PreparedStatement state = cn.prepareStatement("INSERT INTO posts.posts (name, link, text, created)"
                             + " VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING;", Statement.RETURN_GENERATED_KEYS)) {
            state.setString(1, post.getTitle());
            state.setString(2, post.getLink());
            state.setString(3, post.getDescription());
            state.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            state.execute();
            try (ResultSet generatedKey = state.getGeneratedKeys()) {
                if (generatedKey.next()) {
                    post.setId(generatedKey.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cn.prepareStatement("SELECT * FROM posts.posts;")) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(createPost(result));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post foundPost = null;
        try (PreparedStatement statement = cn.prepareStatement("SELECT * FROM posts.posts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    foundPost = createPost(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundPost;
    }

    @Override
    public void close() throws Exception {
        if (cn != null) {
            cn.close();
        }
    }


    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }


    public static void main(String[] args) throws Exception {
        Post first = new Post("one", "http://1", "first", LocalDateTime.parse("2007-01-01T10:11:11"));
        Post second = new Post("two", "http://2", "second", LocalDateTime.parse("2007-02-02T20:22:22"));
        Post third = new Post("three", "http://3", "third", LocalDateTime.parse("2007-03-03T21:33:33"));
        Properties cfg = new Properties();
        try (InputStream in = Grab.class.getClassLoader().getResourceAsStream("habrCareerGrabber.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            throw new FileNotFoundException("Cannot find property file. Please, check file name.");
        }
        Store store = new PsqlStore(cfg);
        store.save(first);
        store.save(second);
        store.save(third);
        store.save(first);
        store.save(first);
        System.out.println(store.findById(2));
        List<Post> posts = store.getAll();
        System.out.println(posts.size());
        posts.forEach(System.out::println);
    }

}