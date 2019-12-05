package com.matthew.clique.models;

public class User {
    public String user_id, first_name, last_name;

    public User() {

    }

    public User(String userId, String first_name, String last_name) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_id = user_id;
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
}
