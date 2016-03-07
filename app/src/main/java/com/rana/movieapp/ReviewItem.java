package com.rana.movieapp;

/**
 * Created by Rana on 12/26/2015.
 */
public class ReviewItem {
    private String id;
    private String author;
    private String content;

    public ReviewItem(String id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
