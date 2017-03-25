package com.menon.rahul.uberapp;

import java.util.Date;

/**
 * Created by Rahul on 9/19/2016.
 */
public class PrivateChat
{

    private String message;
    private String source;
    private long timestamp;

    PrivateChat()
    {}

    PrivateChat(String a, String c)
    {
        this.message = a;
        this.source = c;
        this.timestamp = new Date().getTime();
    }

    protected String getMessageText() {
        return message;
    }

    protected void setMessageText(String messageText) {
        this.message = messageText;
    }

    protected String getMessageUser() {
        return source;
    }

    protected void setMessageUser(String messageUser) {
        this.source = messageUser;
    }

    protected long getMessageTime() {
        return timestamp;
    }

}
