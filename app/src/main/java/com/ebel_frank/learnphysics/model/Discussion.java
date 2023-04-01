package com.ebel_frank.learnphysics.model;

public class Discussion {
    private String discussionId;
    private User author;
    private String question;
    private String imageUrl;
    private long timestamp;

    public Discussion(String discussionId, User author, String question, String imageUrl) {
        this.discussionId = discussionId;
        this.author = author;
        this.question = question;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public Discussion() {
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
    }
}
