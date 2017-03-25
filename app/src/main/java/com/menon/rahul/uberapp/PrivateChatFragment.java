package com.menon.rahul.uberapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.support.v4.app.Fragment;

/**
 * Created by Rahul on 9/20/2016.
 */

public class PrivateChatFragment extends Fragment
{
    Firebase PrivateChatListsmRef = new Firebase("https://fir-uberapp.firebaseio.com/PrivateChatLists");
    ListView mListView;
    Button AddPrivateChatButton;
    String name;
    String emailID;
    private FirebaseAuth mAuth;
    Set<String> Users;
    String[] PrivChatUsers;
    private ProgressDialog mProgressDialog;
    private ListAdapter PrivateChatList;
    private User SignedInUser;
    private UberApp state;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(getActivity());
        Log.e("Entered oncreate view", "yes");
        return inflater.inflate(R.layout.activity_private_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        mListView = (ListView) view.findViewById(R.id.ListView);
        String Key = SignedInUser.getUserName();
        PrivateChatList = new FirebaseListAdapter<String>(getActivity(), String.class, android.R.layout.simple_list_item_1, PrivateChatListsmRef.child(Key))
        {
            @Override
            protected void populateView(View view, String s, int i)
            {
                Log.e("chatlist value", s);
                ((TextView)view.findViewById(android.R.id.text1)).setText(s);
                if(Users.contains(s))
                    Users.remove(s);
            }
        };
        Log.d("key", Key);
        mListView.setAdapter(PrivateChatList);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        state = (UberApp)getActivity().getApplicationContext();
        mAuth = state.getfirebaseAuth();
        //String InstanceID = mAuth.getCurrentUser().getUid();
        SignedInUser = state.getSignedInUser();
        name = SignedInUser.getName();
        emailID = SignedInUser.getEmailID();
        Users = new HashSet<String>();
        //Log.e("Firebase instance ID", InstanceID);

        for(String s: state.getAllUsersEmailUsernames().keySet())
            Users.add(state.getAllUsersEmailUsernames().get(s));

        PrivChatUsers = Users.toArray(new String[Users.size()]);
        Log.e("User array", Arrays.toString(PrivChatUsers));
    }

    @Override
    public void onStart()
    {
        super.onStart();

        AddPrivateChatButton = (Button) getActivity().findViewById(R.id.AddPrivateChatButton);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String ListValue = String.valueOf(adapterView.getItemAtPosition(i));
                Log.e("Destination value",ListValue);
                Intent In = new Intent(getActivity(), ChatWindow.class);
                In.putExtra("Recepient", ListValue);
                In.putExtra("Chat_type", "private");
                startActivity(In);
            }
        });

        assert AddPrivateChatButton != null;
        AddPrivateChatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Users.remove(SignedInUser.getUserName());
                PrivChatUsers = Users.toArray(new String[Users.size()]);
                AlertDialog.Builder PrivChat = new AlertDialog.Builder(getActivity());
                PrivChat.setTitle("New Chat");
                PrivChat.setItems(PrivChatUsers, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Log.e("User selected", PrivChatUsers[i]);
                        PrivateChatListsmRef.child(SignedInUser.getUserName()).push().setValue(PrivChatUsers[i]);
                        PrivateChatListsmRef.child(PrivChatUsers[i]).push().setValue(SignedInUser.getUserName());
                        Intent In = new Intent(getActivity(), ChatWindow.class);
                        String recipient = PrivChatUsers[i];
                        In.putExtra("Recepient", recipient);
                        In.putExtra("Chat_type", "private");
                        startActivity(In);
                    }
                });
                PrivChat.show();
            }
        });
    }


    private void showProgressDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    //-------------------------------------------------------------------------------------------------------------------------
    private void hideProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.hide();
        }
    }
}