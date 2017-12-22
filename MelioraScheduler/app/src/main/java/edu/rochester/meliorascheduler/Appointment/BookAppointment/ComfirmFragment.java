package edu.rochester.meliorascheduler.Appointment.BookAppointment;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Professor;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.Home.HomeActivity.DATE_KEY;
import static edu.rochester.meliorascheduler.Home.HomeActivity.PROF_KEY;
import static edu.rochester.meliorascheduler.Home.HomeActivity.TIME_KEY;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_SCHEDULE_APPOINTMENT;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComfirmFragment extends Fragment {

    public interface ComfirmFragUpdate{
        public void addSelectDateFrag();
    }

    private ComfirmFragUpdate mComfirmFragUpdate;
    private TextView mProfNameTextView;
    private TextView mAppointmentDateTextView;
    private TextView mAppointmentTimeTextView;
    private TextView mSuccessTexView;
    private TextView mComfirmTextView;
    private TextView mReasonTexView;
    private EditText mReasonEditText;
    private Button mSubmitButton;
    private Button mBackButton;
    private View mButtonView;

    private Professor mProfessor;
    private Student mStudent;
    private String mTime;
    private String mDate;
    private String mReason;

    private HttpHandlerThread mHttpHandler;
    //This is used in creating a snackbar
    private View mSnackView;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    public ComfirmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mComfirmFragUpdate = (ComfirmFragUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mComfirmFragUpdate = (ComfirmFragUpdate) activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Start Handler Thread
        setUpHTTPHandler();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comfirm, container, false);

        mSnackView = view;

        //Retrieve selected professor from fragment arguments
        mProfessor = (Professor)getArguments().getSerializable(PROF_KEY);
        //Retrieved Account details(Student) from fragment argument
        mStudent = (Student)getArguments().getSerializable(ACCOUNT_KEY);
        //Retrieve appointment time from fragment argument
        mTime = getArguments().getString(TIME_KEY);
        mDate = getArguments().getString(DATE_KEY);
        Log.d("TEST", "Time:" +mTime);
        Log.d("TEST", "Date: "+mDate);

        mProfNameTextView = (TextView)view.findViewById(R.id.comfirm_profName);
        mAppointmentDateTextView = (TextView)view.findViewById(R.id.comfirm_Date);
        mAppointmentTimeTextView = (TextView)view.findViewById(R.id.comfirm_Time);
        mSubmitButton = (Button)view.findViewById(R.id.comfirm_SubmitButton);
        mSuccessTexView = (TextView)view.findViewById(R.id.comfirm_SuccessTextView);
        mComfirmTextView = (TextView)view.findViewById(R.id.comfirm_ComfirmTextView);
        mReasonTexView = (TextView)view.findViewById(R.id.comfirm_ReasonTextView);
        mReasonEditText = (EditText)view.findViewById(R.id.comfirm_Reason);
        mBackButton = (Button)view.findViewById(R.id.comfirm_backButton);
        mButtonView = (View)view.findViewById(R.id.comfirm_ButtonLayout);

        mProfNameTextView.setText(mProfessor.getName());
        mAppointmentTimeTextView.setText(mTime);

        DateFormat format = new SimpleDateFormat("yyyy-M-d", Locale.US);
        try {
            Date date = format.parse(mDate);
            DateFormat outputFormatter = new SimpleDateFormat("E MMM d, yyyy", Locale.US);
            String dateString = outputFormatter.format(date);
            mAppointmentDateTextView.setText(dateString);
            //Log.d("TEST", "Hello");
        } catch (ParseException e) {
            e.printStackTrace();
        }



        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mReasonEditText.getText().toString().trim()))
                {
                    Toast.makeText(getActivity(), "PLEASE ENTER REASON FOR APPOINTMENT", Toast.LENGTH_LONG).show();
                }
                else{
                        mReason = mReasonEditText.getText().toString().trim();
                    try {
                        if(isConnected()){
                            mJsonRequest.put("authorization", mStudent.getApiKey());
                            mJsonRequest.put("profID", mProfessor.getId());
                            mJsonRequest.put("profName", mProfessor.getName());
                            mJsonRequest.put("stdID", mStudent.getId());
                            mJsonRequest.put("appointmentTime", mTime);
                            mJsonRequest.put("appointmentDate", mDate);
                            mJsonRequest.put("reason", mReason);
                            //Make HTTP connection to API and send Json user data
                            mHttpHandler.processRequest(mJsonRequest, STUDENT_SCHEDULE_APPOINTMENT);
                        }
                        else{
                            Snackbar.make(mSnackView, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //'profID', 'stdID', 'appointmentTime', 'appointmentDate', 'reason'


                }

            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mComfirmFragUpdate.addSelectDateFrag();
            }
        });


        return view;
    }

    //Check if user is connected to internet
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void setUpHTTPHandler()
    {
        Handler responseHandler = new Handler();
        mHttpHandler = new HttpHandlerThread(responseHandler);
        mHttpHandler.setContext(getContext());
        mHttpHandler.setActivity(getActivity());
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
                    //Check for any error
                    if(error){
                        String message = mJsonResponse.getString("message");
                        //Display snackbar with error message
                        makeSnack(mSnackView, message);
                    }
                    else{
                        String message = mJsonResponse.getString("message");
                        mReasonTexView.setText(mReason);
                        mButtonView.setVisibility(View.GONE);
                        mSuccessTexView.setVisibility(View.VISIBLE);
                        mReasonEditText.setVisibility(View.GONE);
                        mReasonTexView.setVisibility(View.VISIBLE);
                        mComfirmTextView.setText("Appointment");
                        makeSnack(mSnackView, message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mHttpHandler.start();
        mHttpHandler.getLooper();
    }

    //Makes a snackbr
    public void makeSnack(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHttpHandler.quit();
    }



}
