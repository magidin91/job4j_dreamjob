package ru.jobj4.dream.store;

import ru.jobj4.dream.model.Post;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    private static final Store INST = new Store();

    private final Map<Integer, Post> posts = new ConcurrentHashMap<>();

    private Store() {
        posts.put(1, new Post(1, "Junior Java Job", "Some description for junior vacancy", new Date()));
        posts.put(2, new Post(2, "Middle Java Job", "Some description for middle vacancy", new Date()));
        posts.put(3, new Post(3, "Senior Java Job", "Some description for senior vacancy", new Date()));
    }

    public static Store instOf() {
        return INST;
    }

    public Collection<Post> findAll() {
        return posts.values();
    }

    public static void main(String[] args) {
        System.out.println(new Date());
    }
}