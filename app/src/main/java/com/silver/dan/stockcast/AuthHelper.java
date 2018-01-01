package com.silver.dan.stockcast;

/**
 * Created by dan on 8/12/17.
 */


import android.content.Context;
import android.support.annotation.NonNull;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.silver.dan.stockcast.callbacks.SimpleCallback;

import java.util.HashSet;
import java.util.Set;

public class AuthHelper {
    private static FirebaseUser user;


    private static JWT firebaseUserAccessToken;
//    private static GoogleToken googleAccessToken;

    private final Context context;


    AuthHelper(Context context) {
        this.context = context;
    }


    void getUserJwt(SimpleCallback<JWT> callback) {
        if (firebaseUserAccessToken == null || firebaseUserAccessToken.isExpired(0)) {
            getFirebaseAccessToken(callback);
        } else {
            callback.onComplete(firebaseUserAccessToken);
        }
    }

    static AuthHelper with(Context ctx) {
        return new AuthHelper(ctx);
    }


    FirebaseUser getFirebaseUser() {
        return user;
    }

    /*
    void hasGrantedScope(final Scope scope, final SimpleCallback<Boolean> callback) {
        GoogleApiClient client = GoogleClientBuilder().build();
        getGrantedScopes(client, new SimpleCallback<Set<Scope>>() {
            @Override
            public void onComplete(Set<Scope> result) {
                callback.onComplete(result.contains(scope));
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    // takes about 1s
    void silentSignIn(final GoogleApiClient googleApiClient, final SimpleCallback<GoogleSignInResult> callback) {
        Log.v(MainActivity.TAG, "silentSignIn()");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleSignInResult googleSignInResult = null;
                try {
                    ConnectionResult result = googleApiClient.blockingConnect();
                    if (result.isSuccess()) {
                        googleSignInResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient).await();
                    }

                } catch (Exception e) {
                    Log.e(MainActivity.TAG, e.toString());
                    callback.onError(e);
                } finally {
                    googleApiClient.disconnect();
                }

                if (googleSignInResult == null) {
                    callback.onError(new Exception("googleSignInResult == null"));
                    return;
                }

                if (googleSignInResult.isSuccess()) {
                    callback.onComplete(googleSignInResult);
                } else {
                    Log.e(MainActivity.TAG, googleSignInResult.getStatus().toString());
                    callback.onError(new Exception(googleSignInResult.getStatus().toString()));
                }
            }
        });
        thread.start();
    }

    // to debug google token - https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=
    void getGoogleAccessToken(final SimpleCallback<String> callback) {

        if (googleAccessToken != null && !googleAccessToken.isExpired()) {
            callback.onComplete(googleAccessToken.getAccessToken());
            return;
        }

        final String firebaseIdToken = user.getIdToken(false).getResult().getToken();
        Ion.with(context)
                .load(context.getString(R.string.APP_URL) + "/getGoogleAccessToken")
                .setBodyParameter("firebaseIdToken", firebaseIdToken)
                .asJsonObject()
                .setCallback(new com.koushikdutta.async.future.FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(e);
                            return;
                        }
                        String jwtString = result.get("access_token").getAsString();

                        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                        calendar.add(Calendar.SECOND, result.get("expires_in").getAsInt());

                        googleAccessToken = new GoogleToken(jwtString, calendar.getTime());

                        callback.onComplete(jwtString);
                    }
                });
    }

    void getGrantedScopes(final GoogleApiClient googleApiClient, final SimpleCallback<Set<Scope>> callback) {
        this.silentSignIn(googleApiClient, new SimpleCallback<GoogleSignInResult>() {
            @Override
            public void onComplete(GoogleSignInResult result) {
                callback.onComplete(result.getSignInAccount().getGrantedScopes());
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    */
    // Configure Google Sign In
    GoogleSignInOptions getGoogleGSO() {
        return getGoogleGSO(new HashSet<Scope>());
    }


    public GoogleSignInOptions getGoogleGSO(Set<Scope> scopes) {
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestServerAuthCode(context.getString(R.string.web_client_id), false);
                //.requestEmail();

        if (scopes != null) {
            for (Scope scope : scopes)
                gsoBuilder.requestScopes(scope);
        }

        return gsoBuilder.build();
    }

    static void setServiceJwt(JWT userJwt) {
        AuthHelper.firebaseUserAccessToken = userJwt;
    }

    void setNewUserInfo(FirebaseUser user) {
        AuthHelper.user = user;
    }
//
//    private void clearGoogleTokenCache() {
//        AuthHelper.googleAccessToken = null;
//    }

    static void signout(GoogleApiClient googleApiClient) {
        FirebaseAuth.getInstance().signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
//                        updateUI(null);
                    }
                });

        // null out statics
//        AuthHelper.googleAccessToken = null;
        AuthHelper.user = null;
        AuthHelper.firebaseUserAccessToken = null;
    }
    /*
    void sendAuthCode(String authCode, final OnCompleteCallback callback) {
        String firebaseIdToken = user.getIdToken(false).getResult().getToken();
        Ion.with(context)
            .load(context.getString(R.string.APP_URL) + "/saveRefreshTokensFromAuthCode")
            .setBodyParameter("serverCode", authCode)
            .setBodyParameter("firebaseIdToken", firebaseIdToken)
            .asString()
            .withResponse()
            .setCallback(new FutureCallback<Response<String>>() {
                @Override
                public void onCompleted(Exception e, Response<String> response) {
                    if (e != null) {
                        callback.onError(e);
                        return;
                    }
                    callback.onComplete();
                }
            });
    }
    */

    private void getFirebaseAccessToken(final SimpleCallback<JWT> jwtCallback) {
        final String firebaseIdToken = user.getIdToken(false).getResult().getToken();
        Ion.with(context)
            .load(context.getString(R.string.APP_URL) + "/getFirebaseAccessToken")
            .setBodyParameter("firebaseIdToken", firebaseIdToken)
            .asJsonObject()
            .setCallback(new com.koushikdutta.async.future.FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (e != null) {
                        jwtCallback.onError(e);
                        return;
                    }
                    String jwtString = result.get("serviceAccessToken").getAsString();
                    JWT jwt = new JWT(jwtString);
                    setServiceJwt(jwt);
                    jwtCallback.onComplete(jwt);
                }
            });
    }

    /*

    GoogleApiClient.Builder GoogleClientBuilder() {
        return new GoogleApiClient
                .Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGoogleGSO());
    }

    GoogleApiClient.Builder GoogleClientWithScopesBuilder(Scope requiredScope) {
        Set<Scope> scopes = new HashSet<>();
        scopes.add(requiredScope);

        return GoogleClientBuilder()
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGoogleGSO(scopes));
    }
    */
}