package com.bounswe2017.group10.atlas.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bounswe2017.group10.atlas.R;
import com.bounswe2017.group10.atlas.home.HomeActivity;
import com.bounswe2017.group10.atlas.util.BlurBuilder;
import com.bounswe2017.group10.atlas.util.Constants;
import com.bounswe2017.group10.atlas.util.Utils;

public class AuthActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState != null) {
            return;
        }
        // if there is a stored token, automatically log in.
        if (Utils.getSharedPref(this).contains(Constants.AUTH_STR)) {
            // refresh token
            String authStr = Utils.getSharedPref(this).getString(Constants.AUTH_STR, Constants.NO_AUTH_STR);
            // TODO: refresh token with token-refresh endpoint.
            // log user in automatically
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        // Do the following operations only when the activity is created for the first time

        // blur layout background
        FrameLayout frameLayout = findViewById(R.id.auth_container);
        Bitmap origBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        Bitmap blurredBitmap = (new BlurBuilder()).blur(this, origBitmap);
        frameLayout.setBackground(new BitmapDrawable(getResources(), blurredBitmap));

        Fragment authMenuFragment = new AuthMenuFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.auth_container, authMenuFragment)
                .commit();
    }

    /**
     * When back button is pressed on login or signup fragments,
     * go back to the previous fragment.
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
