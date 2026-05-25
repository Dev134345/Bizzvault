package com.example.bizvault;

public class Document {
    private String id;
    private String userId;
    private String name;
    private String category;
    private String expiry; // yyyy-MM-dd
    private String path;   // local paths or urls
    private int isImportant; // 0 or 1
    private long timestamp; // Upload/Creation time

    public Document() {
        // Required for Firebase
    }

    public Document(String userId, String name, String category, String expiry, String path, int isImportant, long timestamp) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.expiry = expiry;
        this.path = path;
        this.isImportant = isImportant;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public int getIsImportant() { return isImportant; }
    public void setIsImportant(int isImportant) { this.isImportant = isImportant; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}