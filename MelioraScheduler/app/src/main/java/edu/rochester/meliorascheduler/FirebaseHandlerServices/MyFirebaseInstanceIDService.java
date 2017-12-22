package edu.rochester.meliorascheduler.FirebaseHandlerServices;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.content.ContentValues.TAG;

/**
 * Created by Kennedy Agusi on 11/24/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String mToken;

    public MyFirebaseInstanceIDService(){
        onTokenRefresh();
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d("TEST", "Refreshed token: " + refreshedToken);
        this.mToken = refreshedToken;
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }
}
