package com.matthew.clique.models;

import com.google.firebase.firestore.FieldValue;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

public class Conversation {

    String conversation_id;
    Date time_created;
    Date latest_message;
    List<String> users;

    public Conversation() {

    }

    public Conversation(String conversation_id, Date time_created, Date latest_message, List<String> users) {
        this.conversation_id = conversation_id;
        this.time_created = time_created;
        this.users = users;
        this.latest_message = latest_message;
    }

    public Date getLatest_message() {
        return latest_message;
    }

    public void setLatest_message(Date latest_message) {
        this.latest_message = latest_message;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public Date getTime_created() {
        return time_created;
    }

    public void setTime_created(Date time_created) {
        this.time_created = time_created;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
