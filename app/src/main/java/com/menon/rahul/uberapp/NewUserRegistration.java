package com.menon.rahul.uberapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rahul on 1/16/2017.
 */

public class NewUserRegistration extends AppCompatActivity
{
    protected EditText name;
    protected EditText emailID;
    protected EditText username;
    protected FirebaseUser firebaseuser;
    protected FirebaseDatabase mRef;
    protected HashSet<String> AllUsersUsernames;
    protected AppCompatButton CreateAcctButton;
    protected AppCompatButton SignOutButton;
    protected CoordinatorLayout coordinatorLayout;
    protected View.OnClickListener mUsernameOnClickListener;
    protected View.OnClickListener mCredentialsOnClickListener;
    protected Snackbar snackbar;
    protected UberApp state;
    protected GoogleApiClient mGoogleApiClient;
    private CircleImageView UserProfilePicture;
    private Uri UserProfilePictureUri;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_registration);

        sharedPreferences = this.getSharedPreferences("com.uberapp.stuff", Context.MODE_PRIVATE);
        mRef = FirebaseDatabase.getInstance();
        UserProfilePicture = (CircleImageView)findViewById(R.id.UserProfilePicture);
        name = (EditText) findViewById(R.id.name);
        emailID = (EditText) findViewById(R.id.emailID);
        username = (EditText) findViewById(R.id.username);
        CreateAcctButton = (AppCompatButton)findViewById(R.id.CreateAcctButton);
        SignOutButton = (AppCompatButton)findViewById(R.id.SignOutButton);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        state = (UberApp)getApplicationContext();
        mGoogleApiClient = state.getGoogleApiClient();

        firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseuser!=null)
        {
            name.setText(firebaseuser.getDisplayName());
            emailID.setText(firebaseuser.getEmail());
            UserProfilePictureUri = firebaseuser.getPhotoUrl();
            Log.d("PP URI", UserProfilePictureUri.toString());
            Picasso.with(getApplicationContext()).load(UserProfilePictureUri).error(R.drawable.empty_profile_image).into(UserProfilePicture);
        }
       AllUsersUsernames = new HashSet<String>((state.getAllUsersEmailUsernames()).values());
    }

    @Override
    public void onStart()
    {
        super.onStart();
        AllUsersUsernames = new HashSet<String>((state.getAllUsersEmailUsernames()).values());

        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder NewAcctDiscardConfirmation = new AlertDialog.Builder(NewUserRegistration.this,R.style.MyAlertDialogStyle);
                NewAcctDiscardConfirmation.setTitle("Account Creation");
                NewAcctDiscardConfirmation.setMessage("Start again or quit?");
                NewAcctDiscardConfirmation.setPositiveButton("START AGAIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SignOut();

                        Intent i = new Intent(NewUserRegistration.this, LoginPage.class);
                        i.putExtra("LogOut", "true");
                        startActivity(i);
                        finish();
                    }
                });

                NewAcctDiscardConfirmation.setNegativeButton("QUIT", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SignOut();

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                        startActivity(intent);
                        finish();
                        System.exit(0);
                    }
                });

                NewAcctDiscardConfirmation.show();
            }
        });

        CreateAcctButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String InputUserName = username.getText().toString().toLowerCase().trim();
                if(InputUserName.equals(""))
                {
                    Toast.makeText(getApplication(), "Please enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(name.getText().toString().equals(firebaseuser.getDisplayName()) && emailID.getText().toString().equals(firebaseuser.getEmail()) && !InputUserName.equals(""))
                {
                    if(AllUsersUsernames.contains(InputUserName))
                    {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Username exists, please select another", Snackbar.LENGTH_LONG)
                                .setAction("CLEAR", mUsernameOnClickListener);
                        snackbar.setActionTextColor(Color.RED);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.DKGRAY);
                        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                    }

                    else
                    {
                        AlertDialog.Builder UserNameConfirmation = new AlertDialog.Builder(NewUserRegistration.this,R.style.MyAlertDialogStyle);
                        UserNameConfirmation.setTitle("Username Confirmation");
                        UserNameConfirmation.setMessage("Continue with Username: "+InputUserName);

                        UserNameConfirmation.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                sharedPreferences.edit().putString(firebaseuser.getEmail(), InputUserName).apply();
                                User NewUserCredentials = new User(firebaseuser.getDisplayName(), firebaseuser.getEmail(), InputUserName, new Date().getTime());
                                mRef.getReference().child("Users").push().setValue(NewUserCredentials);
                                Toast.makeText(getApplication(), "Welcome, "+InputUserName, Toast.LENGTH_LONG).show();
                                state.setSignedInUser(NewUserCredentials);
                                Intent i = new Intent(NewUserRegistration.this, MainActivity.class);
                                startActivity(i);
                            }
                        });

                        UserNameConfirmation.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                        UserNameConfirmation.show();
                    }
                }

                else
                {
                    snackbar = Snackbar
                            .make(coordinatorLayout, "INVALID CREDENTIALS", Snackbar.LENGTH_LONG)
                            .setAction("RESET", mCredentialsOnClickListener);
                    snackbar.setActionTextColor(Color.RED);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.DKGRAY);
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });

        mUsernameOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                username.setText(null);
                return;
            }
        };

        mCredentialsOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                name.setText(firebaseuser.getDisplayName());
                emailID.setText(firebaseuser.getEmail());
                return;
            }
        };
    }

    public void SignOut()
    {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>()
                            {
                                @Override
                                public void onResult(Status status)
                                {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(getApplication(), "Signed out from google", Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        SignOut();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }
}