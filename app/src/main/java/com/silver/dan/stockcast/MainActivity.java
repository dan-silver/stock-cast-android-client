package com.silver.dan.stockcast;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.silver.dan.stockcast.callbacks.SimpleCallback;
import com.silver.dan.stockcast.cast.WidgetCastChannel;
import com.silver.dan.stockcast.intro.IntroActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity  {

    @BindView(R.id.top_toolbar)
    Toolbar top_toolbar;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    public static final String TAG = "StockCast";
    private TabSwitcher adapter;

    private MenuItem mediaRouteMenuItem;

    private CastContext mCastContext;
    private CastSession mCastSession;

    private WidgetCastChannel mHelloWorldChannel;

    private Integer appToolbarHeight = null;
    private Integer originalToolbarHeight = null;

    SharedPreferences prefs = null;

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.navigation, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    private SessionManagerListener<CastSession> mSessionManagerListener
            = new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarting(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
            Log.v(TAG, "Session started");
            mCastSession = castSession;
            invalidateOptionsMenu();
            startCustomMessageChannel();
            sendServiceJwt();
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
            // ignore
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
            Log.d(TAG, "Session ended");
            if (mCastSession == castSession) {
                cleanupSession();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {
            // ignore
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {
            // ignore
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
            Log.d(TAG, "Session resumed");
            mCastSession = castSession;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
            // ignore
        }
    };

    private void sendMessage(String message) {
        if (mCastSession != null && mHelloWorldChannel != null) {
            mHelloWorldChannel.sendMessage(mCastSession, message);
        }
    }

    private void startCustomMessageChannel() {
        if (mCastSession != null && mHelloWorldChannel == null) {
            mHelloWorldChannel = new WidgetCastChannel(getString(R.string.cast_namespace));
            try {
                mCastSession.setMessageReceivedCallbacks(mHelloWorldChannel.getNamespace(),
                        mHelloWorldChannel);
                Log.d(TAG, "Message channel started");
            } catch (IOException e) {
                Log.d(TAG, "Error starting message channel", e);
                mHelloWorldChannel = null;
            }
        }
    }

    private void sendServiceJwt() {
        AuthHelper.with(getApplicationContext()).getUserJwt(new SimpleCallback<JWT>() {
            @Override
            public void onComplete(JWT result) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("ServiceJwt", result.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendMessage(obj.toString());
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("com.silver.dan.stockcast", MODE_PRIVATE);

        ButterKnife.bind(this);

        // Set a Toolbar to replace the ActionBar.
        setSupportActionBar(top_toolbar);


        adapter = new TabSwitcher(getSupportFragmentManager());

        // add fragments as pages
        adapter.addFragment(new StockListFragment());
        adapter.addFragment(new AppSettingsTheme());
        adapter.addFragment(new AccountSettingsFragment());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_reorder_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_settings_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_person_white_24dp);

        mCastContext = CastContext.getSharedInstance(this);

    }

    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (resultCode != RESULT_OK) {
            return;
        }

        Log.v(MainActivity.TAG, "unhandled activity result");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show app intro only on first launch
        if (prefs.getBoolean("firstrun", true)) {
            Intent appIntroIntent = new Intent(this, IntroActivity.class);
            startActivity(appIntroIntent);

            prefs.edit().putBoolean("firstrun", false).apply();
        }

        // Register cast session listener
        mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener,
                CastSession.class);
        if (mCastSession == null) {
            // Get the current session if there is one
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister cast session listener
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener,
                CastSession.class);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupSession();
    }

    private void cleanupSession() {
        closeCustomMessageChannel();
        mCastSession = null;
    }

    public void setMultiSelectMode(boolean inMultiSelectMode) {
        tabLayout.setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE);

        if (appToolbarHeight == null) {
            appToolbarHeight = top_toolbar.getHeight();
        }

        if (originalToolbarHeight == null) {
            originalToolbarHeight = ((AppBarLayout.LayoutParams) top_toolbar.getLayoutParams()).height;
        }

        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) top_toolbar.getLayoutParams();

        if (inMultiSelectMode) {
            layoutParams.height = appToolbarHeight * 2 - 5;
        } else {
            layoutParams.height = originalToolbarHeight;
        }

        top_toolbar.setLayoutParams(layoutParams);
    }

    private void closeCustomMessageChannel() {
        if (mCastSession != null && mHelloWorldChannel != null) {
            try {
                mCastSession.removeMessageReceivedCallbacks(mHelloWorldChannel.getNamespace());
                Log.d(TAG, "Message channel closed");
            } catch (IOException e) {
                Log.d(TAG, "Error closing message channel", e);
            } finally {
                mHelloWorldChannel = null;
            }
        }
    }
}