package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    /* создали DataSource, который работает с пулом соединений commons-dbcp2 */
    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties properties = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("src/main/resources/db.properties")
        )) {
            properties.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        /* настриваем DataSource */
        pool.setDriverClassName(properties.getProperty("jdbc.driver"));
        pool.setUrl(properties.getProperty("jdbc.url"));
        pool.setUsername(properties.getProperty("jdbc.username"));
        pool.setPassword(properties.getProperty("jdbc.password"));
        /*  устанавливаем мин и макс соединений */
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM candidate")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Candidate(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post);
        } else {
            update(post);
        }
    }

    private Post create(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("INSERT INTO post(name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();
            try (ResultSet rsl = ps.getGeneratedKeys()) {
                if (rsl.next()) {
                    post.setId(rsl.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    private void update(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE post Set Name = ?", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("Select* from post where id= ?")
        ) {
            ps.setInt(1, id);
            try (ResultSet rsl = ps.executeQuery()){
                if (rsl.next()) {
                    post = new Post(id, rsl.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }
}