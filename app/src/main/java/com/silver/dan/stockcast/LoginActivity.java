package com.silver.dan.stockcast;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;

    @BindView(R.id.login_loading_spinner)
    ProgressBar loadingSpinner;

    private GoogleApiClient mGoogleApiClient;

    private int RC_SIGN_IN = 10000;

    @BindView(R.id.sign_in_button)
    com.google.android.gms.common.SignInButton signInButton;

    AuthHelper authHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authHelper = new AuthHelper(this);

        ButterKnife.bind(this);

        setLoadingUI(false);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, authHelper.getGoogleGSO())
                .enableAutoManage(this, this)
                .build();

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    return;
                }

                setLoadingUI(true);
                authHelper.setNewUserInfo(user);
                userFinishedAuth();

            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

//        setLoadingUI(true); // UI trickery, by default try to silently auth
        mAuth = FirebaseAuth.getInstance();
        /*authHelper.silentSignIn(mGoogleApiClient, new SimpleCallback<GoogleSignInResult>() {
            @Override
            public void onComplete(GoogleSignInResult result) {
                setLoadingUI(true);
                firebaseAuthWithGoogle(result);
            }

            @Override
            public void onError(Exception e) {
                setLoadingUI(false);
            }
        });*/
    }

    @OnClick(R.id.sign_in_button)
    public void signin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void setLoadingUI(final boolean loadingUI) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingUI) {
                    loadingSpinner.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.GONE);
                } else {
                    loadingSpinner.setVisibility(View.GONE);
                    signInButton.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                setLoadingUI(true);
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result);
            } else {
                try {
                    Log.e(MainActivity.TAG, result.getStatus().toString());
                } catch (Exception e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                }
                // Google Sign In failed, update UI appropriately
                setLoadingUI(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInResult gso) {

        // get scopes with result.getSignInAccount().getGrantedScopes()

        AuthCredential credential = GoogleAuthProvider.getCredential(gso.getSignInAccount().getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        authHelper.setNewUserInfo(user);
                        userFinishedAuth();
                        /*authHelper.sendAuthCode(gso.getSignInAccount().getServerAuthCode(), new OnCompleteCallback() {
                            @Override
                            public void onComplete() {
                                userFinishedAuth();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                setLoadingUI(false);
                            }
                        });*/
                    } else {
                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        setLoadingUI(false);
                    }
                }
            });


    }

    private void userFinishedAuth() {
        launchMainActivity();
    }

    private void launchMainActivity() {
        stopGoogleApiClient();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        setLoadingUI(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopGoogleApiClient();
    }

    private void stopGoogleApiClient() {
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }
}