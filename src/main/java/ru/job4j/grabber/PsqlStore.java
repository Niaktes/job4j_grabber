package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cn;

    public PsqlStore(Connection connection) {
        this.cn = connection;
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

}