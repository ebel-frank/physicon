package com.ebel_frank.learnphysics.model;


public class Comment {
    private String commentId;
    private String senderId;
    private String comment;
    private String imageUrl;
    private long timestamp;

    //Used by Firebase
    public Comment() {
    }

    public Comment(String commentId, String senderId, String comment, String imageUrl) {
        this.commentId = commentId;
        this.senderId = senderId;
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getComment() {
        return comment;
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
}
