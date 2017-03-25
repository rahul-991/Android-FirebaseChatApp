package com.menon.rahul.uberapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;

import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.util.Set;

/**
 * Created by Rahul on 05/03/2017.
 */
public class ChatMessageRecyclerAdapter extends FirebaseObjectRecyclerAdapter<ChatMessage,MessageViewHolder>
{

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;

    private String UserName;
    public Set<String> positionSet;
    private Context context;

    public ChatMessageRecyclerAdapter(String MyUserName, DatabaseReference ref, Context c, Set<String> p)
    {
        super(ChatMessage.class, R.layout.message, MessageViewHolder.class, ref);
        this.UserName = MyUserName;
        context = c;
        this.positionSet = p;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;

        switch (viewType)
        {
            case LEFT_MSG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left, parent, false);
                return new MessageViewHolder(view);

            case RIGHT_MSG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right, parent, false);
                return new MessageViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position)
    {
        ChatMessage model = getItem(position);
        if (model.getUserName().equals(UserName))
            return RIGHT_MSG;
        else
            return LEFT_MSG;
    }

    @Override
    protected void populateViewHolder(MessageViewHolder viewHolder, ChatMessage model, int position)
    {
        viewHolder.message.setText(model.getText());
        viewHolder.author.setText(model.getUserName());
        viewHolder.timestamp.setText(DateFormat.getInstance().format(model.getTimestamp()));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position)
    {
        ChatMessage chatMessage = getItem(position);
        viewHolder.author.setText(chatMessage.getUserName());
        viewHolder.timestamp.setText(DateFormat.getInstance().format(chatMessage.getTimestamp()));
        viewHolder.message.setText(chatMessage.getText());
        if(positionSet!=null)
        {
            Log.d("position", String.valueOf(position));
            Log.d("hashset",positionSet.toString());
            if (positionSet.contains(chatMessage.getUserName()+": "+chatMessage.getText()))
                viewHolder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }
}