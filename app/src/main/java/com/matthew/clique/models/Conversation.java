package com.matthew.clique.models;

import java.util.List;

public class Conversation {

    String conversation_id, time_created;
    List<String> users;

    public Conversation() {

    }

    public Conversation(String conversation_id, String time_created, List<String> users) {
        this.conversation_id = conversation_id;
        this.time_created = time_created;
        this.users = users;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getTime_created() {
        return time_created;
    }

    public void setTime_created(String time_created) {
        this.time_created = time_created;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
