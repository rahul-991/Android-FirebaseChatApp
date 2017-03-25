package com.menon.rahul.uberapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import junit.framework.Test;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Rahul on 8/26/2016.
 */
public class ChatWindow extends AppCompatActivity
{
    private Firebase mRef;
    private EditText Message;
    private ImageButton SendButton;
    private RecyclerView mRecyclerView;
    private DatabaseReference ref;
    private String ChatRecepient;
    private String FirebaseDBGroup;
    private String FirebaseDBPrivate;
    private String MyUserName;
    private String ChatType;
    private String emailID;
    private UberApp state;
    private User SignedInUser;
    private Button AnalyzeButton;
    private BackgroundAlchemyService backgroundAlchemyService;
    private Context context;
    private Toolbar toolbar;
    private StringBuilder AnalyzeString;
    private Window window;
    private ChatMessageRecyclerAdapter adapter;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean isMultiSelect;
    private Set<String> positionSet;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_window);
        state = (UberApp) getApplicationContext();
        context = this;
        isMultiSelect = true;
        SignedInUser = state.getSignedInUser();
        Message = (EditText)findViewById(R.id.MessageText);
        SendButton = (ImageButton)findViewById(R.id.SendButton);
        AnalyzeButton = (Button)findViewById(R.id.AnalyzeButton);
        toolbar = (Toolbar) findViewById(R.id.ChatWindow_toolbar);
        positionSet = new HashSet<>();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        window = this.getWindow();
        mLinearLayoutManager = new LinearLayoutManager(this);

        emailID = SignedInUser.getEmailID();
        mRecyclerView = (RecyclerView) findViewById(R.id.messagesList);
        ChatRecepient = getIntent().getStringExtra("Recepient");
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setTitle(ChatRecepient);

        FirebaseDBGroup = "https://fir-uberapp.firebaseio.com/GroupMessages/"+ChatRecepient;
        MyUserName = SignedInUser.getUserName();
        FirebaseDBPrivate = "https://fir-uberapp.firebaseio.com/PrivateChat/";
        ChatType = getIntent().getStringExtra("Chat_type");

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.holo_blue_dark));

        AnalyzeString = new StringBuilder();

        if (ChatType.equals("group"))
        {
            ref = FirebaseDatabase.getInstance().getReference().child("GroupMessages").child(ChatRecepient);
            adapter = new ChatMessageRecyclerAdapter(MyUserName, ref, this, positionSet);
        }

        else if (ChatType.equals("private"))
        {
            String Key;
            if (MyUserName.compareToIgnoreCase(ChatRecepient) < 0)
                Key = MyUserName + "<>" + ChatRecepient;
            else
                Key = ChatRecepient + "<>" + MyUserName;
            ref = FirebaseDatabase.getInstance().getReference().child("PrivateChat").child(Key);
            adapter = new ChatMessageRecyclerAdapter(MyUserName, ref, this, positionSet);
        }

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                if(isMultiSelect)
                {
                    ChatMessage chatMessage = adapter.getItem(position);
                    String key = chatMessage.getUserName()+": "+chatMessage.getText();
                    if(positionSet.contains(key))
                        positionSet.remove(key);
                    else
                    {
                        positionSet.add(key);
                        Log.d("else condition", "added");
                    }
                    return;
                }

                Toast.makeText(getApplicationContext(), "Long Click to activate: "+String.valueOf(position), Toast.LENGTH_SHORT);
                refreshAdapter();
            }

            @Override
            public void onItemLongClick(View view, int position)
            {
                isMultiSelect = true;
                ChatMessage chatMessage = adapter.getItem(position);
                String key = chatMessage.getUserName()+": "+chatMessage.getText();
                Toast.makeText(ChatWindow.this, "Long Click on position: "+position, Toast.LENGTH_SHORT).show();
                if(positionSet.contains(key))
                    positionSet.remove(key);
                else
                {
                    positionSet.add(key);
                    Log.d("else condition", "added");
                }

                refreshAdapter();
            }
        }));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        SendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ChatType.equals("group"))
                {
                    mRef = new Firebase(FirebaseDBGroup);
                    if (!Message.getText().toString().trim().equals(""))
                    {
                        ChatMessage chatMessage = new ChatMessage(MyUserName, Message.getText().toString());
                        mRef.push().setValue(chatMessage);
                        Message.setText(null);
                    }
                }

                else if (ChatType.equals("private"))
                {
                    mRef = new Firebase(FirebaseDBPrivate);
                    if (!Message.getText().toString().trim().equals(""))
                    {
                        ChatMessage chatMessage = new ChatMessage(MyUserName, Message.getText().toString().trim());
                        ref.push().setValue(chatMessage);
                        Message.setText(null);
                    }
                }

                mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        AnalyzeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isMultiSelect = false;
                Log.d("Analyze String", AnalyzeString.toString());
                backgroundAlchemyService = new BackgroundAlchemyService(context);
                backgroundAlchemyService.execute(AnalyzeString);
                AnalyzeString = new StringBuilder();
            }
        });
    }

    public void refreshAdapter()
    {
        adapter.positionSet = positionSet;
        adapter.notifyDataSetChanged();
    }
//----------------------------------------------------------------------------------------------------------
}