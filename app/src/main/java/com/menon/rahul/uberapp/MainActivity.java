package com.menon.rahul.uberapp;

/**
 * Created by Rahul on 22/02/2017.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private Firebase mRef = new Firebase("https://fir-uberapp.firebaseio.com/Users");
    private GoogleApiClient mGoogleApiClient;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private TextView user_emailID;
    private TextView user_fullname;
    private TextView user_username;
    private CircleImageView user_profilepic;
    private String name;
    private String emailID;
    private ArrayList<String> Users;
    private UberApp state;
    private FirebaseAuth mAuth;
    private User SignedInUser;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView) ;
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new PagerAdapter()).commit();
        window = this.getWindow();
        state = (UberApp)getApplicationContext();
        Users = new ArrayList<String>();
        SignedInUser = state.getSignedInUser();
        mAuth = state.getfirebaseAuth();
        mGoogleApiClient = state.getGoogleApiClient();
        emailID = SignedInUser.getEmailID();
        name = SignedInUser.getName();
        Uri photoUri=mAuth.getCurrentUser().getPhotoUrl();
        View v = mNavigationView.getHeaderView(0);
        user_profilepic = (CircleImageView) v.findViewById(R.id.user_profilepic);
        user_emailID = (TextView) v.findViewById(R.id.user_emailID);
        user_fullname = (TextView) v.findViewById(R.id.user_fullname);
        user_username = (TextView) v.findViewById(R.id.user_username);
        user_fullname.setText(name);
        user_emailID.setText(emailID);
        user_username.setText(state.getSignedInUser().getUserName());
        Picasso.with(getApplicationContext()).load(photoUri).error(R.drawable.empty_profile_image).into(user_profilepic);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                mDrawerLayout.closeDrawers();
                if(menuItem.getItemId() == R.id.sign_out_button)
                    SignOut();

                return false;
            }
        });

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public void SignOut()
    {
        Log.e("Entered sign out code","true");
        mGoogleApiClient.connect();
        Log.d("Client at signout", (String.valueOf(mGoogleApiClient.isConnected())));
        state.connectGoogleApi();
        Log.d("state at signout", (String.valueOf(state.getGoogleApiClient().isConnected())));
        Toast.makeText(getApplication(), "Signing Out..", Toast.LENGTH_LONG).show();

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                FirebaseAuth.getInstance().signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    mRef.unauth();
                                    mAuth.signOut();
                                    state.setfirebaseAuth(mAuth);
                                    Toast.makeText(getApplication(), "Signed out from google", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        });

        Intent i = new Intent(MainActivity.this, LoginPage.class);
        i.putExtra("LogOut", "true");
        startActivity(i);
        finish();
    }
//-------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //finish();
        //System.exit(0);
    }
}