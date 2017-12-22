package edu.rochester.meliorascheduler.Home;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Appointment;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.crash.FirebaseCrash.setCrashCollectionEnabled;
import static edu.rochester.meliorascheduler.Home.CurrentAppointmentDialogueActivity.APPOINTMENT_KEY;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_VIEW_UPCOMING_APPOINTMENT;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public interface HomeFragUpdate{
        public void addBookAppointment();
        public void addAppointmentDialogue(Appointment appointment);
    }

    private HomeFragUpdate mHomeFragUpdate;
    private RecyclerView mUpcomingAppRecyclerView;
    private TextView mNoUpcomingTextView;
    private TextView mUpcomingTextView;
    private View mUpcomingLayout;
    private Button mBookAppointmentButton;
    private Student mStudent;

    private AppointmentAdapter mAdapter;
    private List<Appointment> mAppointmentList = new ArrayList<>(1);

    private HttpHandlerThread mHttpHandler;
    //This is used in creating a snackbar
    private View mSnackView;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    //Used to change color of Recycler view
    int colorss[] = new int[2];
    int a = 1;

    private int mAdapterPosition;

    private FirebaseAnalytics mFirebaseAnalytics;


    //Use to determine when to hide Recylcerview header
    boolean isHeaderShown = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        setUpHTTPHandler();

        mHomeFragUpdate = (HomeFragUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mHomeFragUpdate = (HomeFragUpdate) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        //Start Handler Thread
        setUpHTTPHandler();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setCrashCollectionEnabled(true);
        FirebaseCrash.log("Home Fragment created");


        mSnackView = view;

        colorss[0] = getResources().getColor(R.color.rowColor1);
        colorss[1] = getResources().getColor(R.color.rowColor2);

        //Retrieved Account details(Student) from fragment argument
        mStudent = (Student)getArguments().getSerializable(ACCOUNT_KEY);

        mNoUpcomingTextView = (TextView)view.findViewById(R.id.home_NoUpcomingAppointmentTextView);
        mUpcomingTextView = (TextView)view.findViewById(R.id.home_UpcomingTextView);
        mUpcomingLayout = (View)view.findViewById(R.id.home_upcomingLayout);
        mBookAppointmentButton = (Button)view.findViewById(R.id.home_bookAppButton);

        mAdapter = new AppointmentAdapter(mAppointmentList);
        mUpcomingAppRecyclerView = (RecyclerView) view.findViewById(R.id.home_UpcomingAppointmentRecycleView);
        mUpcomingAppRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUpcomingAppRecyclerView.setAdapter(mAdapter);

        mBookAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHomeFragUpdate.addBookAppointment();
            }
        });

        getCurrentAppointments();

        return view;
    }

    public void getCurrentAppointments(){
        try {
            if(isConnected()){
                mJsonRequest.put("authorization", mStudent.getApiKey());
                mJsonRequest.put("stdID", mStudent.getId());
                //Make HTTP connection to API and send Json user data
                mHttpHandler.processRequest(mJsonRequest, STUDENT_VIEW_UPCOMING_APPOINTMENT);
            }
            else{
                makeSnack(mSnackView, "You are NOT connected to internet");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    //Update Adapter array and display list of professor's schedule in Recycler view
                    updateAdapterArray();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mHttpHandler.start();
        mHttpHandler.getLooper();
    }


    public void updateAdapterArray() throws JSONException {
        boolean found = mJsonResponse.getBoolean("Found");
        if(found){
            mUpcomingLayout.setVisibility(View.VISIBLE);
            mNoUpcomingTextView.setVisibility(View.GONE);

            JSONArray appointments = mJsonResponse.getJSONArray("Appointments");
            for(int i = 0; i<appointments.length(); i++){
                JSONObject appointmentJson = appointments.getJSONObject(i);
                int appointmentID = appointmentJson.getInt("appointmentID");
                int profID = appointmentJson.getInt("prof_id");
                String profName = appointmentJson.getString("profName");
                String profEmail = appointmentJson.getString("profEmail");
                String profOfficeLoc = appointmentJson.getString("profOfficeLoc");
                String appointTime = appointmentJson.getString("appointment_time");
                String appointDate = appointmentJson.getString("appointment_date");
                String reason = appointmentJson.getString("reason_for_appointment");

                Appointment appoint = new Appointment();
                appoint.setAppointmentID(appointmentID);
                appoint.setProfID(profID);
                appoint.setProfName(profName);
                appoint.setProfEmail(profEmail);
                appoint.setProfOfficeLoc(profOfficeLoc);
                appoint.setAppointmentTime(appointTime);
                appoint.setAppointmentDate(appointDate);
                appoint.setReasoForAppointment(reason);
                mAppointmentList.add(appoint);
            }
            mAdapter.notifyDataSetChanged();
        }
        else{
            mUpcomingLayout.setVisibility(View.GONE);
            mNoUpcomingTextView.setVisibility(View.VISIBLE);
        }

        //mUnCheckedTimeList = mTimeList;
    }

    //Makes a snackbr
    public void makeSnack(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    //Adapter for professor's schedule time recycle view
    public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentHolder>  {

        private List<Appointment> adaptAppointment;

        public AppointmentAdapter(List<Appointment> appointmentList)
        {
            adaptAppointment = appointmentList;

        }

        @Override
        public AppointmentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.current_appoint_display, parent, false);
            return new AppointmentHolder(view);
        }

        @Override
        public void onBindViewHolder(AppointmentHolder holder, int position) {
            holder.bind(adaptAppointment.get(position));
            // mAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return adaptAppointment.size();
        }
    }


    public class AppointmentHolder extends RecyclerView.ViewHolder  {

        View mHeaderView;
        TextView mAppointmentWithTextView;
        TextView mDateTextView;
        TextView mCancelTextView;

        Appointment holderAppointment;


        public AppointmentHolder(View itemView) {
            super(itemView);


            a ^= 1;
            //Change background color of itemView
            itemView.setBackgroundColor(colorss[a]);

            mHeaderView = (View)itemView.findViewById(R.id.currentAppoint_Header);
            mAppointmentWithTextView = (TextView)itemView.findViewById(R.id.home_AppointmentWithTextview);
            mDateTextView = (TextView)itemView.findViewById(R.id.home_dateTextView);
            mCancelTextView = (TextView)itemView.findViewById(R.id.home_CancelTextView);

            mCancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isHeaderShown = false;
                    mHeaderView.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(getActivity(), CancelAppointDialogueActivity.class);
                    intent.putExtra(APPOINTMENT_KEY, holderAppointment);
                    intent.putExtra(ACCOUNT_KEY, mStudent);
                    startActivityForResult(intent, 100);
                    mAdapterPosition = getAdapterPosition();

                }
            });

            mDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mHomeFragUpdate.addAppointmentDialogue(holderAppointment);
                    Intent intent = new Intent(getActivity(), CurrentAppointmentDialogueActivity.class);
                    intent.putExtra(APPOINTMENT_KEY, holderAppointment);
                    startActivity(intent);
                }
            });

            mAppointmentWithTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mHomeFragUpdate.addAppointmentDialogue(holderAppointment);
                    Intent intent = new Intent(getActivity(), CurrentAppointmentDialogueActivity.class);
                    intent.putExtra(APPOINTMENT_KEY, holderAppointment);
                    startActivity(intent);

                }
            });


        }

        public void bind(Appointment appointment) {
            holderAppointment = appointment;

            if(isHeaderShown){
                mHeaderView.setVisibility(View.GONE);
            }
            else{
                isHeaderShown = true;
            }

            mAppointmentWithTextView.setText(holderAppointment.getProfName());
            mDateTextView.setText(holderAppointment.getAppointmentDate());

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            makeSnack(mSnackView, "Appointment has been successfully cancelled");
            mAppointmentList.remove(mAdapterPosition);
            mAdapter.notifyDataSetChanged();
        }

    }



    @Override
    public void onResume(){
        super.onResume();
        setUpHTTPHandler();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHttpHandler.quit();
    }

}
