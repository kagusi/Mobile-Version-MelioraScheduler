package edu.rochester.meliorascheduler.Login;


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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_LOGIN;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_LOGIN_BYAPI;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public interface LoginFragUpdate{
        public void loginSuccess(Student account);
        public void addForgotPassFrag();
        public void addCreateAccountFrag();
    }

    private LoginFragUpdate mLoginFragUpdate;
    private EditText mUsernameEdiText;
    private EditText mPasswordEdiText;
    private TextView mForgotPassTextView;
    private TextView mCreateAccountTextView;
    private Button mLoginButton;

    private String mUsername;
    private String mPassword;
    private String mApiKey;
    private Student mStudent;
    private String mStatus;

    //Used to determine whether a user logged out during last app usage
    public static final String SHAREDPREF_STATUSKEY = "shared_statusKey";
    public static final String SHAREDPREF_APIKEY = "shared_apiKey";

    //This is used in creating a snackbar
    private View mSnackView;

    private HttpHandlerThread mHttpHandler;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mLoginFragUpdate = (LoginFragUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mLoginFragUpdate = (LoginFragUpdate) activity;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

         mUsernameEdiText = (EditText)view.findViewById(R.id.Login_UserIDEditText);
         mPasswordEdiText = (EditText)view.findViewById(R.id.Login_PasswordEditText);
         mForgotPassTextView = (TextView)view.findViewById(R.id.Login_ForgotPassTextView);
         mCreateAccountTextView = (TextView)view.findViewById(R.id.Login_CreateAccountTextView);
         mLoginButton = (Button)view.findViewById(R.id.Login_SubmitButton);


            //Check whether current user has used this app previously and didnt logout
            //If so, user session will be restored
            /*
            if(mApiKey != null && mStatus != null)
            {
                if(mStatus.equals("LoggedIn"))
                    restoreState();
            }
            */


         mLoginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(TextUtils.isEmpty(mUsernameEdiText.getText().toString().trim()))
                 {
                     Toast.makeText(getActivity(), "PLEASE ENTER YOUR USERNAME", Toast.LENGTH_LONG).show();
                 }
                 else if(TextUtils.isEmpty(mPasswordEdiText.getText().toString().trim())) {
                     Toast.makeText(getActivity(), "PLEASE ENTER YOUR PASSWORD", Toast.LENGTH_LONG).show();

                 }
                 else{
                     mUsername = mUsernameEdiText.getText().toString().trim();
                     mPassword = mPasswordEdiText.getText().toString().trim();

                     try {
                         mJsonRequest.put("email", mUsername);
                         mJsonRequest.put("password", mPassword);
                         mJsonRequest.put("authorization", "none");

                         //Check whether user is connected to internet
                         if(isConnected()){
                             mSnackView = view;
                             //Make HTTP connection to API and send Json user data
                             mHttpHandler.setContext(getContext());
                             mHttpHandler.processRequest(mJsonRequest, STUDENT_LOGIN);
                         }
                         else{
                             Snackbar.make(view, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                                     .setAction("Action", null).show();
                         }

                     } catch (JSONException e) {
                         e.printStackTrace();
                     }

                 }
             }
         });

         mForgotPassTextView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mLoginFragUpdate.addForgotPassFrag();
             }
         });

         mCreateAccountTextView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mLoginFragUpdate.addCreateAccountFrag();
             }
         });

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
                        mLoginFragUpdate.loginSuccess(mStudent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                mHttpHandler.processRequest(mJsonRequest, STUDENT_LOGIN_BYAPI);
            }
            else{
                Toast.makeText(getActivity(), "You are NOT connected to internet", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
