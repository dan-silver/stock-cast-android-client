package com.silver.dan.stockcast;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountSettingsFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.userDisplayName)
    TextView userDisplayName;


    @BindView(R.id.userEmail)
    TextView userEmail;

    @BindView(R.id.userPhoto)
    ImageView userPhoto;
    private AuthHelper authHelper;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        ButterKnife.bind(this, view);


        FirebaseUser user = authHelper.getFirebaseUser();

        userDisplayName.setText(user.getDisplayName());
        userEmail.setText(user.getEmail());


        if (user.getPhotoUrl() != null) {
            Ion.with(getContext())
                    .load(user.getPhotoUrl().toString())
                    .withBitmap()
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .intoImageView(userPhoto);

        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new AuthHelper(getContext());

        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, authHelper.getGoogleGSO())
                .enableAutoManage(getActivity(), this)
                .build();

    }

    @OnClick(R.id.user_log_out_btn)
    public void logout() {
        AuthHelper.signout(mGoogleApiClient);
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // @todo
    }
}