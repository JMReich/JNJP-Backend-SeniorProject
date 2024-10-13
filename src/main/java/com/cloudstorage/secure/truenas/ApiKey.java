package com.cloudstorage.secure.truenas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiKey {
    private int id;
    private String name;
    private CreatedAt created_at;
    private String key;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreatedAt getCreated_at() {
        return created_at;
    }

    public void setCreated_at(CreatedAt created_at) {
        this.created_at = created_at;
    }

    public String getKey() {
        return key;
    }
    public
    void setKey(String key) {
        this.key = key;
    }
}

class CreatedAt {
    @JsonProperty("$date")
    private long date;

    // Getters and setters
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
