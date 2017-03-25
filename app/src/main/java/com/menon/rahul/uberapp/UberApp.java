package com.menon.rahul.uberapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.service.AlchemyService;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;


/**
     * Created by Rahul on 8/10/2016.
     */
    public class UberApp extends Application
    {

        private GoogleApiClient mGoogleApiClient;
        private HashMap<String, String> AllUsersEmailUsernames =  new HashMap<String, String>();;
        private User SignedInUser;
        private FirebaseAuth firebaseAuth;
        public boolean value;
        private ChildEventListener UsersEventListener;
        private FirebaseDatabase mRef;
        private AlchemyLanguage AlchemyService;

        @Override
        public void onCreate()
        {
            super.onCreate();
            Firebase.setAndroidContext(this);
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
            AlchemyService = new AlchemyLanguage();
            AlchemyService.setApiKey("1466204b05c468a3015a993e2f3d6b139134f6fa");

            mRef = FirebaseDatabase.getInstance();

            UsersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    Log.d("uberapp state", String.valueOf(AllUsersEmailUsernames.isEmpty()));
                    //Log.d("Login Page datasnapshot", dataSnapshot.toString());
                    User u = dataSnapshot.getValue(User.class);
                    Log.d("state values", u.getEmailID()+u.getUserName());
                    AllUsersEmailUsernames.put(u.getEmailID(),u.getUserName());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mRef.getReference().child("Users").addChildEventListener(UsersEventListener);
        }

        public AlchemyLanguage getAlchemyService()
        {
            return this.AlchemyService;
        }

        public void setGoogleApiClient(GoogleApiClient g)
        {
            this.mGoogleApiClient = g;
        }

        public GoogleApiClient getGoogleApiClient()
        {
            return this.mGoogleApiClient;
        }

        public void connectGoogleApi()
        {
            this.mGoogleApiClient.connect();
        }

        public void disconnectGoogleApi()
        {
            this.mGoogleApiClient.disconnect();
        }

        public boolean getGoogleApiStatus()
        {
            return (this.mGoogleApiClient.isConnected());
        }

        public User getSignedInUser()
        {
            return this.SignedInUser;
        }

        public void setSignedInUser(User u)
        {
            SignedInUser = u;
        }

        public void setfirebaseAuth(FirebaseAuth u)
        {
            this.firebaseAuth = u;
        }

        public FirebaseAuth getfirebaseAuth()
        {
            return this.firebaseAuth;
        }

        public void setAllUsersEmailUsernames(HashMap<String, String> a)
        {
            AllUsersEmailUsernames = a;
        }

        public HashMap<String, String> getAllUsersEmailUsernames()
        {
            return this.AllUsersEmailUsernames;
        }

     }
