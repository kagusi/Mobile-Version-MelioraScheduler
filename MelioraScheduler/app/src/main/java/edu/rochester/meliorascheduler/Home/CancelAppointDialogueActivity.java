package edu.rochester.meliorascheduler.Home;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Appointment;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.Home.CurrentAppointmentDialogueActivity.APPOINTMENT_KEY;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_CANCEL_APPOINTMENT;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;

public class CancelAppointDialogueActivity extends Activity {

    private EditText mReasonEditTextView;
    private Button mCancelAppointmentButton;
    private Appointment mAppointment;
    private Student mStudent;

    private HttpHandlerThread mHttpHandler;
    //This is used in creating a snackbar
    private View mSnackView;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cancel_popout_display);

        mAppointment = (Appointment)getIntent().getSerializableExtra(APPOINTMENT_KEY);
        mStudent = (Student)getIntent().getSerializableExtra(ACCOUNT_KEY);

        mReasonEditTextView = (EditText)findViewById(R.id.cancel_ReasonEdiText);
        mCancelAppointmentButton = (Button)findViewById(R.id.cancel_SubmitButton);

        mCancelAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSnackView = view;
                if(TextUtils.isEmpty(mReasonEditTextView.getText().toString().trim()))
                {
                    Toast.makeText(getApplicationContext(), "PLEASE ENTER REASON FOR CANCELLATION", Toast.LENGTH_LONG).show();
                }
                else{
                    cancelAppointment();
                }

            }
        });
    }

    //Cancel selected appointment by making http request to API
    public void cancelAppointment(){
        try {
            if(isConnected()){
                mJsonRequest.put("authorization", mStudent.getApiKey());
                Log.d("TEST", "API-KEY: "+mStudent.getApiKey());
                mJsonRequest.put("reason", mReasonEditTextView.getText());
                mJsonRequest.put("cancelledBy", "Student");
                mJsonRequest.put("appointment_id", mAppointment.getAppointmentID());
                //Make HTTP connection to API and send Json user data
                mHttpHandler.processRequest(mJsonRequest, STUDENT_CANCEL_APPOINTMENT);
            }
            else{
                makeSnack(mSnackView, "You are NOT connected to internet");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Makes a snackbr
    public void makeSnack(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    //Check if user is connected to internet
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
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
                    if(error){
                        makeSnack(mSnackView, mJsonResponse.getString("message"));
                    }
                    else{
                        setResult(RESULT_OK);
                        finish();
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
    public void onResume() {
        super.onResume();
        setUpHTTPHandler();
    }
}
