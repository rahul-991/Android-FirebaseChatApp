package com.menon.rahul.uberapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Rahul on 9/29/2016.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder
{
    protected TextView author;
    protected TextView message;
    protected TextView timestamp;
    protected LinearLayout linearLayout;

    public MessageViewHolder(View v)
    {
        super(v);
        author = (TextView)v.findViewById(R.id.author);
        message = (TextView)v.findViewById(R.id.message);
        timestamp = (TextView)v.findViewById(R.id.timestamp);
        linearLayout = (LinearLayout)v.findViewById(R.id.message_chat);
    }
}
