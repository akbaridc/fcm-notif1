package com.example.pelaporanbakesbangpolmalang.configfile;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class MyFirebaseInstanceIdService {
    private static final String REG_TOKEN = "REG_TOKEN";

    public void onTokenRefresh(String token) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN, "Refreshed token Baru: " + token);

//         If you want to send messages to this application instance or
//         manage this apps subscriptions on the server side, send the
//         Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {
    }
}
