package com.menon.rahul.uberapp;

import java.util.Date;

/**
 * Created by Rahul on 9/12/2016.
 */
public class ChatMessage
{
    private String UserName;
    private String Text;
    private long timestamp;

    ChatMessage(String u, String m)
    {
        this.UserName = u;
        this.Text = m;
        this.timestamp = new Date().getTime();
    }

    ChatMessage()
    {    }

    protected String getText() {
        return Text;
    }

    protected void setText(String Text) {
        this.Text = Text;
    }

    protected String getUserName() {
        return this.UserName;
    }

    protected void setUserName(String UserName) {
        this.UserName = UserName;
    }

    protected long getTimestamp() {
        return timestamp;
    }

    /*
    public boolean equals(Object obj)
    {
        if(!(obj instanceof ChatMessage))
            return false;
        else if(obj == this)
            return true;

        return (this.Text.equals(((ChatMessage) obj).Text) && this.timestamp == ((ChatMessage) obj).timestamp && this.UserName.equals(((ChatMessage) obj).UserName));
    }

    public int hashCode()
    {
        return (this.UserName.hashCode()+this.Text.hashCode());
    }
    */
}

//-----------------------------------------------------------------------------------------------------------
