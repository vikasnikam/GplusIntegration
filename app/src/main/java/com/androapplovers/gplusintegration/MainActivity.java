package com.androapplovers.gplusintegration;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener ,
GoogleApiClient.ConnectionCallbacks{
    public  static String TAG="MainActivity";
    //Signin button
    private SignInButton signInButton;

    // Sign out Button
    private Button sign_out_button;
    //Signing Options
    private GoogleSignInOptions gso;

    //google api client
    private GoogleApiClient mGoogleApiClient;

    //Signin constant to check the activity result
    private int RC_SIGN_IN = 100;

    //TextViews
    private TextView textViewName;
    private TextView textViewEmail;
    private NetworkImageView profilePhoto;

    //Image Loader
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initializing Views
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        profilePhoto = (NetworkImageView) findViewById(R.id.profileImage);
        sign_out_button=(Button)findViewById(R.id.sign_out_button);
        //Initializing google signin option
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_ME), new Scope(Scopes.PLUS_LOGIN),new Scope(Scopes.PROFILE))
                .build();

        //Initializing signinbutton
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        //Initializing google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();


        //Setting onclick listener to signing button
        signInButton.setOnClickListener(this);
        // Setting onClick Listener to Signout button
        sign_out_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == signInButton) {
            //Calling signin
            signIn();
        }else if(v == sign_out_button){
            //Calling sign out
            signOut();
        }
    }



    //This function will option signing intent
    private void signIn() {
      //  profilePhoto.setVisibility(View.VISIBLE);

        //Creating an intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        //Starting intent for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If signin
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
            if (result.isSuccess()) {
                mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            }
            // G+
            if (mGoogleApiClient.hasConnectedApi(Plus.API)) {
                Person person  = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                if (person != null) {
                    Log.i(TAG, "--------------------------------");
                    Log.i(TAG, "Display Name: " + person.getDisplayName());
                    Log.i(TAG, "Gender: " + person.getGender());
                    Log.i(TAG, "About Me: " + person.getAboutMe());
                    Log.i(TAG, "Birthday: " + person.getBirthday());
                    Log.i(TAG, "Current Location: " + person.getCurrentLocation());
                    Log.i(TAG, "Language: " + person.getLanguage());
                } else {
                    Log.e(TAG, "Error!");
                }
            } else {
                Log.e(TAG, "Google+ not connected");
            }
        }
    }
    //After the signing we are calling this function
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("TAG","GoogleSignInAccount="+acct);

            //Displaying name and email
            textViewName.setText(acct.getDisplayName());
            textViewEmail.setText(acct.getEmail());


           /* //Initializing image loader
            imageLoader = CustomVolleyRequest.getInstance(this.getApplicationContext())
                    .getImageLoader();

            imageLoader.get(acct.getPhotoUrl().toString(),
                    ImageLoader.getImageListener(profilePhoto,
                            R.mipmap.ic_launcher,
                            R.mipmap.ic_launcher));

            //Loading image
            profilePhoto.setImageUrl(acct.getPhotoUrl().toString(), imageLoader);*/

        } else {
            //If login fails
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }


    // Sign Out
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d("TAG","status="+status);

                        Toast.makeText(MainActivity.this, "LogOutSuccessfully", Toast.LENGTH_SHORT).show();
                        textViewName.setText("");
                        textViewEmail.setText("");
                        profilePhoto.setDefaultImageResId(R.mipmap.ic_launcher);

                }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);
       // addGoogleProfile(currentPerson); //Add the user to a list of persons
       // refreshListView(); //update the listView
        Log.d(TAG, "Current person: name=" + currentPerson.getDisplayName() + ", has birthday = " + (currentPerson.hasBirthday() ? "yes, it is" + currentPerson.getBirthday() : "no"));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
