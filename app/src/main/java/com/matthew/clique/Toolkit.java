package com.matthew.clique;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Toolkit extends AppCompatActivity {
    private Activity activity;

    public Toolkit(@Nullable Activity activity) {
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

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State connectedState = NetworkInfo.State.CONNECTED;
        NetworkInfo.State mobileConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifiConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        return (mobileConnection == connectedState || wifiConnection == connectedState);
    }

    public String generateUID(int length) {
        Random rand = new Random();
        String values = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(values.charAt(rand.nextInt(values.length())));
        }

        return stringBuilder.toString();
    }

    public String convertDateToTime(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return sdf.format(date);
        } else {
            return "";
        }
    }
}
