package com.matthew.clique.models;

import java.util.Date;

public class Message {
    public String message_id, conversation_id, sender, message;
    public Date time_sent;

    public Message() {

    }

    public Message(String message_id, String conversation_id, String sender, String message, Date time_sent) {
        this.message_id = message_id;
        this.conversation_id = conversation_id;
        this.sender = sender;
        this.message = message;
        this.time_sent = time_sent;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(Date time_sent) {
        this.time_sent = time_sent;
    }
}
