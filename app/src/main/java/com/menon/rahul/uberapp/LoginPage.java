package com.menon.rahul.uberapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.menon.rahul.uberapp.R;
import com.facebook.FacebookSdk;
import android.app.ProgressDialog;

import java.util.HashMap;
import java.util.HashSet;


public class LoginPage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{

    private LoginButton FBloginButton;
    private CallbackManager callbackManager;
    private ProgressDialog mProgressDialog;
    private SignInButton google_login_button;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LoginPage";
    private FirebaseDatabase mRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String LogOutCheck;
    private boolean check;
    private UberApp state;
    protected  HashMap<String, String> AllUsersEmailIDs;
    protected FirebaseUser firebaseUser;
    private ChildEventListener UsersEventListener;
    private  SharedPreferences sharedPreferences;
    private Window window;


    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        window = this.getWindow();
        mRef = FirebaseDatabase.getInstance();
        check=true;
        state = (UberApp) getApplicationContext();
        AllUsersEmailIDs = state.getAllUsersEmailUsernames();
        LogOutCheck = getIntent().getStringExtra("LogOut");
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.holo_blue_light));
        if(LogOutCheck == null)
            LogOutCheck = "false";

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getApplication().getSharedPreferences("com.uberapp.stuff", Context.MODE_PRIVATE);

//----------------------------------------- GOOGLE SIGN- IN ----------------------------------------------------------------------------------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        //AppEventsLogger.activateApp(this);
        if(state.getGoogleApiClient()==null)
        {
            Log.e("Google Api Client", "new");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            mGoogleApiClient.connect();
            state.setGoogleApiClient(mGoogleApiClient);
            state.connectGoogleApi();
        }

//------------------------------------------ END -------------------------------------------------------------------------------
        google_login_button = (SignInButton) findViewById(R.id.google_login_button);
        google_login_button.setSize(SignInButton.SIZE_STANDARD);
        google_login_button.setScopes(gso.getScopeArray());
        findViewById(R.id.google_login_button).setOnClickListener(this);

// ------------------------------------------------- FACEBOOK SIGN-IN -----------------------------------------------------------------------------------------------------------------------------------
        /*if (Profile.getCurrentProfile() != null)
        {
            Toast.makeText(getApplication(), "Welcome through facebook, " + Profile.getCurrentProfile().getName(), Toast.LENGTH_LONG).show();
            Intent i = new Intent(LoginPage.this, MainActivity.class);
            i.putExtra("name",Profile.getCurrentProfile().getName());
            i.putExtra("emailID",Profile.getCurrentProfile().getId());
            i.putExtra("Provider","facebook");
            startActivity(i);
        }*/
        callbackManager = CallbackManager.Factory.create();
        FBloginButton = (LoginButton) findViewById(R.id.fb_login_button);
        FBloginButton.setReadPermissions("email","public_profile");
        FBloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                handleFacebookAccessToken(loginResult.getAccessToken());
                if(check)
                {
                    Toast.makeText(getApplication(), "Welcome through Facebook, " + Profile.getCurrentProfile().getName(), Toast.LENGTH_LONG).show();
                    Intent i = new Intent(LoginPage.this, MainActivity.class);
                    i.putExtra("name", Profile.getCurrentProfile().getName());
                    i.putExtra("emailID", Profile.getCurrentProfile().getId());
                    i.putExtra("provider", "facebook");
                    startActivity(i);
                }
                else
                    Toast.makeText(getApplication(), "Facebook login error", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancel()
            {
                Toast.makeText(getApplication(), "Facebook login Cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException e)
            {
                Toast.makeText(getApplication(), "Unsuccessful login attempt, try again", Toast.LENGTH_LONG).show();
            }
        });


//------------------------------ ------------------------------- END -----------------------------------------------------------------------------------------------------------------------------------



//--------------------------------------------------------------- FIREBASE AUTHENTICATION LISTENER -----------------------------------------------------------------------------------------------------
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    state.setfirebaseAuth(firebaseAuth);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else
                {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }
//--------------------------------------------------------------- END --------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.i("Request code is ", Integer.toString(requestCode));
        Log.i("Data value is ", data.toString());
        if (requestCode == 9001)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account, result);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct, final GoogleSignInResult result)
    {
        //Log.d(TAG, "Firebase authentication with google:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Log.d("credential value",credential.toString());
        showProgressDialog();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        //Log.d(TAG, "From function firebaseAuthWithGoogle: " + task.isSuccessful());

                        if (!task.isSuccessful())
                        {
                            //Log.w(TAG, "From function firebaseAuthWithGoogle: ", task.getException());
                            Toast.makeText(LoginPage.this, "Google Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                        handleSignInResult(result);
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d("LoginPage", "handleSignInResult:" + result.isSuccess());

        if (result.isSuccess())
        {
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Log.d("Hashmap size", String.valueOf(AllUsersEmailIDs.isEmpty()));

            for(String s:AllUsersEmailIDs.keySet())
                Log.d("key and value", s+AllUsersEmailIDs.get(s));

            Log.d("Existing", firebaseUser.getEmail());
            if(sharedPreferences.getString(firebaseUser.getEmail(),"notfound").equals("notfound") && !AllUsersEmailIDs.containsKey(firebaseUser.getEmail()))
            {
                Log.d("if first", sharedPreferences.getString(firebaseUser.getEmail(),"notfound"));
                Log.d("if Second", String.valueOf(AllUsersEmailIDs.containsKey(firebaseUser.getEmail())));
                Toast.makeText(getApplication(), "Welcome through google, " + firebaseUser.getEmail(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(LoginPage.this, NewUserRegistration.class);
                startActivity(i);
            }

            else
            {
                Log.d("else first", sharedPreferences.getString(firebaseUser.getEmail(),"notfound"));
                Log.d("else Second", String.valueOf(AllUsersEmailIDs.get(firebaseUser.getEmail())));
                String User_name = AllUsersEmailIDs.get(firebaseUser.getEmail());
                if(User_name == null)
                    User_name = sharedPreferences.getString(firebaseUser.getEmail(),"notfound");
                sharedPreferences.edit().putString(firebaseUser.getEmail(),User_name).apply();
                Toast.makeText(getApplication(), "Welcome again " + User_name, Toast.LENGTH_LONG).show();
                User SignedInUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), sharedPreferences.getString(firebaseUser.getEmail(),"notfound"),0);//, firebaseUser.getPhotoUrl());
                state.setSignedInUser(SignedInUser);
                Intent i = new Intent(LoginPage.this, MainActivity.class);
                startActivity(i);
            }
        }
    }


    private void handleFacebookAccessToken(AccessToken token)
    {
        Log.d(TAG, "Facebook access token:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Facebook sign in: " + task.isSuccessful());
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "Facebook sign in failed: ", task.getException());
                            Toast.makeText(LoginPage.this, "Facebook Authentication failed",Toast.LENGTH_SHORT).show();
                            check = false;
                        }
                    }
                });
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.google_login_button)
        {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(state.getGoogleApiClient());
            mGoogleApiClient = state.getGoogleApiClient();
            startActivityForResult(signInIntent, 9001);
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------
    private void showProgressDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
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

    //------------------------------------------------------------------------------------------------------------------------- CORRECT


    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)

    {
        Log.d("LoginPage", "onConnectionFailed:" + connectionResult);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onStart()
    {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(state.getGoogleApiClient());
        mAuth.addAuthStateListener(mAuthListener);
        AllUsersEmailIDs = state.getAllUsersEmailUsernames();
        //mRef.getReference().child("Users").addChildEventListener(UsersEventListener);
        Log.e("logoutcheck value: ", LogOutCheck);

        if(LogOutCheck.equals("false"))
        {
            if (opr.isDone())
            {
                Log.d("LoginPage", "Got cached sign-in");
                Log.d("Size check start()", String.valueOf(AllUsersEmailIDs));
                GoogleSignInResult result = opr.get();
                Log.i("onStart true value", result.toString());
                handleSignInResult(result);
            }

            else
            {
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>()
                {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult)
                    {
                        Log.i("onStart false value", googleSignInResult.toString());
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("onResume", "true");
        UsersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Log.d("afterchildeventlistener", String.valueOf(AllUsersEmailIDs.isEmpty()));
                //Log.d("Login Page datasnapshot", dataSnapshot.toString());
                User u = dataSnapshot.getValue(User.class);
                Log.d("values", u.getEmailID()+u.getUserName());
                AllUsersEmailIDs.put(u.getEmailID(),u.getUserName());
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
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }



//-------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        //System.exit(0);
    }
}