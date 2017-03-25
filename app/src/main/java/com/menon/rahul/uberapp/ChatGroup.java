package com.menon.rahul.uberapp;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Rahul on 9/14/2016.
 */
public class ChatGroup
{
    private String groupName;
    private ArrayList<String> groupUsers;
    private long createdTimeStamp;
    private long updatedTimeStamp;

    ChatGroup()
    {}

    ChatGroup(String name)
    {
        this.groupName = name;
        this.createdTimeStamp = new Date().getTime();
        this.updatedTimeStamp = new Date().getTime();
    }

    String getGroupName()
    {
        return this.groupName;
    }

    void setGroupUsers(ArrayList<String> groupMembers)
    {
        this.groupUsers = groupMembers;
    }

    ArrayList<String> getGroupUsers()
    {
        return this.groupUsers;
    }

    long getUpdatedTimeStamp()
    {
        return this.updatedTimeStamp;
    }

    void setUpdatedTimeStamp(long updatedTS)
    {
        this.updatedTimeStamp = updatedTS;
    }

}
