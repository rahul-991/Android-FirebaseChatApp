package com.menon.rahul.uberapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.support.v4.app.Fragment;

/**
 * Created by Rahul on 9/20/2016.
 */

public class GroupChatFragment extends Fragment
{
    //private Firebase GroupmRef = new Firebase("https://fir-uberapp.firebaseio.com/Groups");
    private ListView mListView;
    private FirebaseAuth mAuth;
    String name;
    String emailID;
    private Button AddGroupChatButton;
    private List<String> Users;
    private String[] GroupUsers;
    private ArrayList<Integer> NewGroupUsers = new ArrayList<>();
    private ListAdapter adapter;
    private ArrayList<String> UsersGroups = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private UberApp state;
    private User SignedInUser;
    private String NewGroupName;
    private String MyUserName;
    private DatabaseReference ref;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(getActivity());
        Log.e("Entered oncreate view","yes");
        return inflater.inflate(R.layout.activity_group_chat, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        mListView = (ListView) v.findViewById(R.id.ListView);
        ref = FirebaseDatabase.getInstance().getReference().child("Groups");
        adapter = new FirebaseObjectListAdapter<ChatGroup>
                (getActivity(), ChatGroup.class, android.R.layout.simple_list_item_1, ref)
        {
            @Override
            protected void populateView(View view, ChatGroup group, int i)
            {
                ((TextView)view.findViewById(android.R.id.text1)).setText(group.getGroupName());
                if (group.getGroupUsers().contains(MyUserName))
                    UsersGroups.add(group.getGroupName());
            }
        };
        mListView.setAdapter(adapter);

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        state = (UberApp)getActivity().getApplicationContext();
        SignedInUser = state.getSignedInUser();
        mAuth = state.getfirebaseAuth();
        Log.d("UID", mAuth.getCurrentUser().getUid());
        name = SignedInUser.getName();
        emailID = SignedInUser.getEmailID();
        MyUserName = SignedInUser.getUserName();
        Users = new ArrayList<String>();

        for(String s:state.getAllUsersEmailUsernames().keySet())
            Users.add(state.getAllUsersEmailUsernames().get(s));
        Users.remove(MyUserName);
        GroupUsers = Users.toArray(new String[Users.size()]);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Log.d("UID", mAuth.getCurrentUser().getUid());
        AddGroupChatButton = (Button) getActivity().findViewById(R.id.AddGroupChatButton);
        /*
        Log.e("ONstart mref child done","yes");
        Log.e("SetAdapter","beginning");
        //final String MyUserName = SignedInUser.getUserName();
        Log.e("MyName value", MyUserName);
        Log.e("UsersGroups value", UsersGroups.toString());
        Log.e("Path value", GroupmRef.getKey());
        */

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                ChatGroup ListValue = (ChatGroup)adapterView.getItemAtPosition(i);
                Log.e("List value",ListValue.getGroupName());
                if (UsersGroups.contains(ListValue.getGroupName()))
                {
                    Intent In = new Intent(getActivity(), ChatWindow.class);
                    In.putExtra("Recepient", ListValue.getGroupName());
                    In.putExtra("Chat_type", "group");
                    startActivity(In);
                }

                else
                    Toast.makeText(getActivity().getApplication(), "Unauthorized entry", Toast.LENGTH_LONG).show();
            }
        });

        assert AddGroupChatButton != null;
        AddGroupChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder CreateGroup = new AlertDialog.Builder(getActivity());
                CreateGroup.setTitle("Create New Group");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                CreateGroup.setMessage("Group Name ");
                CreateGroup.setView(input);


                CreateGroup.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        NewGroupUsers = new ArrayList<Integer>();
                        NewGroupName = input.getText().toString();
                        final ChatGroup CG = new ChatGroup(NewGroupName);

                        AlertDialog.Builder AddUsers = new AlertDialog.Builder(getActivity());
                        AddUsers.setTitle("Group Members");
                        AddUsers.setMultiChoiceItems(GroupUsers, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b)
                            {
                                if (b)
                                    NewGroupUsers.add(i);
                                else if (NewGroupUsers.contains(i))
                                    NewGroupUsers.remove(Integer.valueOf(i));
                            }
                        });

                        AddUsers.setPositiveButton("ADD Members", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                ArrayList<String> temp = new ArrayList<String>();
                                for(Integer i: NewGroupUsers)
                                    temp.add(Users.get(i));
                                temp.add(MyUserName);
                                CG.setGroupUsers(temp);
                                FirebaseDatabase.getInstance().getReference().child(NewGroupName).setValue(CG);
                                Log.e("Users array for group", CG.getGroupUsers().toString());
                            }

                        });

                        AddUsers.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });

                        AddUsers.show();
                    }
                });

                CreateGroup.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                CreateGroup.show();
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
