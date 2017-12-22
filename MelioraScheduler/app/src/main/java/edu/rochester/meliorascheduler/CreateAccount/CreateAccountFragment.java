package edu.rochester.meliorascheduler.CreateAccount;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import edu.rochester.meliorascheduler.FirebaseHandlerServices.MyFirebaseInstanceIDService;
import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateAccountFragment extends Fragment {

    public interface CreateAccountUpdate{
        public void accountCreated();
    }

    private CreateAccountUpdate mCreateAccountUpdate;
    private EditText mNameEditText;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mComfirmPassEditText;
    private Button mCreateAccountButton;

    private String mName;
    private String mUsername;
    private String mPassword;
    private String mComfirmpass;
    private String mDeviceToken;

    //This is used in creating a snackbar
    private View mSnackView;

    private HttpHandlerThread mHttpHandler;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        setUpHTTPHandler();

        mCreateAccountUpdate = (CreateAccountUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCreateAccountUpdate = (CreateAccountUpdate) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);

        setUpHTTPHandler();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_create_account, container, false);

        mNameEditText = (EditText)view.findViewById(R.id.CreatAccount_NameEditText);
        mUsernameEditText = (EditText)view.findViewById(R.id.CreatAccount_EmailEditText);
        mPasswordEditText = (EditText)view.findViewById(R.id.CreatAccount_PasswordEditText);
        mComfirmPassEditText = (EditText)view.findViewById(R.id.CreatAccount_ComfirmPasswordEditText);
        mCreateAccountButton = (Button)view.findViewById(R.id.CreateAccount_SubmitButton);



        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(mNameEditText.getText().toString().trim()))
                {
                    Toast.makeText(getActivity(), "PLEASE ENTER YOUR NAME", Toast.LENGTH_LONG).show();
                }
                else if(TextUtils.isEmpty(mUsernameEditText.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "PLEASE ENTER USERNAME", Toast.LENGTH_LONG).show();

                }
                else if(TextUtils.isEmpty(mPasswordEditText.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "PLEASE ENTER PASSWORD", Toast.LENGTH_LONG).show();
                }
                else if(TextUtils.isEmpty(mComfirmPassEditText.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "PLEASE COMFIRM PASSWORD", Toast.LENGTH_LONG).show();
                }
                else {


                    mName = mNameEditText.getText().toString().trim();
                    mUsername = mUsernameEditText.getText().toString().trim();
                    mPassword = mPasswordEditText.getText().toString().trim();
                    mComfirmpass = mComfirmPassEditText.getText().toString().trim();

                    //Retrieve api key for this device
                    MyFirebaseInstanceIDService tokenRetriver = new MyFirebaseInstanceIDService();
                    mDeviceToken = tokenRetriver.getToken();

                    if(mPassword.equals(mComfirmpass)) {

                        try {
                            mJsonRequest.put("name", mName);
                            mJsonRequest.put("email", mUsername);
                            mJsonRequest.put("password", mPassword);
                            mJsonRequest.put("api_key", mDeviceToken);
                            mJsonRequest.put("authorization", "none");

                            //Check whether user is connected to internet
                            if(isConnected()){
                                mSnackView = view;
                                //Make HTTP connection to API and send Json user data
                                mHttpHandler.processRequest(mJsonRequest, STUDENT_CREATE);
                            }
                            else{
                                Snackbar.make(view, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                            //JSONArray jsa = new JSONArray();
                            //jsa.put("Blur");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        Toast.makeText(getActivity(), "PASSWORD DOES NOT MATCH", Toast.LENGTH_LONG).show();
                    }

                }

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
                //Get http response from Handler thread
                mJsonResponse = mHttpHandler.getResponse();

                try {
                    boolean error = mJsonResponse.getBoolean("error");
                    String messag = mJsonResponse.getString("message");
                    if(error){

                        makeSnack(mSnackView, messag);
                    }
                    else{
                        Toast.makeText(getActivity(), messag, Toast.LENGTH_LONG).show();
                        scheduleTimer();
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

    public void scheduleTimer()
    {

        //Loop for 5 seconds
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mCreateAccountUpdate.accountCreated();

                    }
                });
            }

        },1500);
    }
}
