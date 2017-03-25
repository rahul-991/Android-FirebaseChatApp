package com.menon.rahul.uberapp;

import android.net.Uri;

/**
 * Created by Rahul on 9/13/2016.
 */

public class User
{
    private String name;
    private String emailID;
    private String userName;
    private long timeStampCreated;

    public User() {}

    public User(String n, String e, String u, long t)
    {
        this.name = n;
        this.emailID = e;
        this.userName = u;
        this.timeStampCreated = t;
    }

    protected String getName()
    {
        return (this.name);
    }

    protected String getEmailID()
    {
        return (this.emailID);
    }

    protected String getUserName()
    {
        return (this.userName);
    }

    protected long getTimeStampCreated()
    {
        return (this.timeStampCreated);
    }
}
