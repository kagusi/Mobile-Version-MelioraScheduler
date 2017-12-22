package edu.rochester.meliorascheduler.ForgotPassword;


import android.app.Activity;
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

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_RECOVER_PASS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPassFragment extends Fragment {

    private EditText mForgotPassEditText;
    private Button mSubmitButton;

    //This is used in creating a snackbar
    private View mSnackView;

    private HttpHandlerThread mHttpHandler;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    public ForgotPassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);

        setUpDownLoadHandler();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_pass, container, false);

        mForgotPassEditText= (EditText)view.findViewById(R.id.ForgotPass_EmailEditText);
        mSubmitButton = (Button)view.findViewById(R.id.ForgotPass_SubmitButton);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mForgotPassEditText.getText().toString().trim()))
                {
                    Toast.makeText(getActivity(), "PLEASE EMAIL ASSOCIATED WITH YOUR ACCOUNT", Toast.LENGTH_LONG).show();
                }
                else{

                    //Check whether user is connected to internet
                    if(isConnected()){
                        mSnackView = view;

                        try {
                            //Retrieve user's email
                            String email = mForgotPassEditText.getText().toString().trim();
                            mJsonRequest.put("email", email);
                            mJsonRequest.put("authorization", "none");
                            //Make HTTP connection to API and send Json user data
                            mHttpHandler.processRequest(mJsonRequest, STUDENT_RECOVER_PASS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Snackbar.make(view, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
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

    public void setUpDownLoadHandler()
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
                    makeSnack(mSnackView, messag);
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

}
