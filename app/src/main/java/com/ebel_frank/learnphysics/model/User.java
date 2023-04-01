package com.ebel_frank.learnphysics.model;

public class User {
    private String _id;
    private String username;
    private String email;
    private String bgColor;

    public User(String _id, String username, String email) {
        this._id = _id;
        this.username = username;
        this.email = email;
    }

    public User() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
