package com.rana.movieapp;

/**
 * Created by Rana on 12/26/2015.
 */
public class VideoItem {
    private String id;
    private String key;
    private String name;
    private String type;

    public VideoItem(String id, String key, String name, String type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }
}
