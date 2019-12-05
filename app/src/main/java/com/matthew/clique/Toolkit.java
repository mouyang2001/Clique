package com.matthew.clique;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Toolkit extends AppCompatActivity {
    private Activity activity;

    public Toolkit(@NonNull Activity activity) {
        this.activity = activity;
    }

    public void closeKeyboard() {
        View view = this.activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean isConnected() { //Todo might still work in event listeners
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State connectedState = NetworkInfo.State.CONNECTED;
        NetworkInfo.State mobileConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifiConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        return (mobileConnection == connectedState || wifiConnection == connectedState);
    }
}
