package edu.rochester.meliorascheduler.SplashScreen;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_LOGIN_BYAPI;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashScreenFragment extends Fragment {

    public interface SplashScreenUpdate{
        public void addLoginPage();
        public void loginSuccess(Student account);
    }

    private SplashScreenUpdate mSplashScreenUpdate;
    private String mUsername;
    private String mPassword;
    private String mApiKey;
    private Student mStudent;
    private String mStatus;

    private HttpHandlerThread mHttpHandler;

    //This is used in creating a snackbar
    private View mSnackView;

    //Used to determine whether a user logged out during last app usage
    public static final String SHAREDPREF_STATUSKEY = "shared_statusKey";
    public static final String SHAREDPREF_APIKEY = "shared_apiKey";


    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mSplashScreenUpdate = (SplashScreenUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mSplashScreenUpdate = (SplashScreenUpdate)activity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);

        setUpHTTPHandler();
        //Retrieve user data from sharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.mStatus = prefs.getString(SHAREDPREF_STATUSKEY, null);
        this.mApiKey = prefs.getString(SHAREDPREF_APIKEY, null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_splash_screen, container, false);

        mSnackView = view;
        //Check whether current user has used this app previously and didnt logout
        //If so, user session will be restored
        if(mApiKey != null && mStatus != null)
        {

            if(mStatus.equals("LoggedIn"))
                restoreState();
            else
                scheduleTimer();
        }
        else
            scheduleTimer();


        return view;
    }


    //Check if user is connected to internet
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void setUpHTTPHandler()
    {
        Handler responseHandler = new Handler();
        mHttpHandler = new HttpHandlerThread(responseHandler);
        mHttpHandler.setHttpProgressListener(new HttpHandlerThread.HttpProgressListener() {

            @Override
            public void someWorkCompleted(Integer work) {

            }

            @Override
            public void jobComplete() {
                //Retrieve http response from Handler thread
                mJsonResponse = mHttpHandler.getResponse();

                if(mJsonResponse != null){

                    try {


                        boolean error = mJsonResponse.getBoolean("error");
                        //Check for successful login
                        if(error){
                            String message = mJsonResponse.getString("message");
                            //Display snackbar with error message
                            makeSnack(mSnackView, message);
                        }
                        else{
                            mStudent = new Student();
                            JSONArray messag = mJsonResponse.getJSONArray("message");
                            JSONObject data = messag.getJSONObject(0);
                            mStudent.setId(data.getInt("stdID"));
                            mStudent.setName(data.getString("name"));
                            mStudent.setEmail(data.getString("email"));
                            mStudent.setApiKey(data.getString("api_key"));

                            //Log.d("TEST", data.getString("dateCreated"));
                            //Redirect user to welcome page
                            mSplashScreenUpdate.loginSuccess(mStudent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }
        });
        mHttpHandler.start();
        mHttpHandler.getLooper();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHttpHandler.quit();
    }

    public void makeSnack(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }


    //Used to restore user session, if user did not logout on last access
    public void restoreState()
    {
        try {
            mJsonRequest.put("api_key", mApiKey);
            mJsonRequest.put("authorization", mApiKey);

            //Check whether user is connected to internet
            if(isConnected()){
                //Make HTTP connection to API and send Json user data
                mHttpHandler.setContext(getContext());
                mHttpHandler.processRequest(mJsonRequest, STUDENT_LOGIN_BYAPI);
            }
            else{
                Toast.makeText(getActivity(), "You are NOT connected to internet", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void scheduleTimer()
    {

        //Loop for 5 seconds
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mSplashScreenUpdate.addLoginPage();

                    }
                });
            }

        },1500);
    }

}
