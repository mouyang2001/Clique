package com.matthew.clique.models;

public class User {
    public String user_id, first_name, last_name, profile_image;

    public User() {

    }

    public User(String userId, String first_name, String last_name, String profile_image) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_id = user_id;
        this.profile_image = profile_image;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUser_id() {return user_id;}

    public void setUser_id(String user_id) {this.user_id = user_id;}

    public String getProfile_image() {return profile_image;}

    public void setProfile_image(String profile_image) {this.profile_image = profile_image;}
}
