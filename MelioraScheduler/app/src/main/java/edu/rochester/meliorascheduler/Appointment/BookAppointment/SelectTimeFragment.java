package edu.rochester.meliorascheduler.Appointment.BookAppointment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Professor;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.Models.Time;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.Home.HomeActivity.DATE_KEY;
import static edu.rochester.meliorascheduler.Home.HomeActivity.PROF_KEY;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_GET_PROF_SCHEDULE;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectTimeFragment extends Fragment {

    public interface SelectTimeUpdate{
        public void addSelectDateFrag(Professor prof);
        public void addComfirmFrag(String time);
    }

    private SelectTimeUpdate mSelectTimeUpdate;
    private TextView mProfNameTextview;
    private RecyclerView mTimeRecyclerView;
    private Button mBackButton;
    private Button mContinueButton;

    private TimeAdapter mAdapter;
    private List<Time> mTimeList = new ArrayList<>(1);

    private HttpHandlerThread mHttpHandler;
    //This is used in creating a snackbar
    private View mSnackView;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    private Time mSelectedTime;
    private Professor mProfessor;
    private Student mStudent;
    private String mTimString;
    private String mDateString;

    private int mLastChecked = -1;

    //Used to change color of Recycler view
    int colorss[] = new int[2];
    int a = 1;

    //Used to detect whether user checked atleast one checkbox
    boolean mSomethingSelected = false;

    //Used to reset checked button in recylerview
    private List<Time> mUnCheckedTimeList;

    public SelectTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mSelectTimeUpdate = (SelectTimeUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mSelectTimeUpdate = (SelectTimeUpdate) activity;
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
        View view = inflater.inflate(R.layout.fragment_select_time, container, false);

        colorss[0] = getResources().getColor(R.color.rowColor1);
        colorss[1] = getResources().getColor(R.color.rowColor2);

        //Retrieve selected professor from fragment arguments
        mProfessor = (Professor)getArguments().getSerializable(PROF_KEY);
        //Retrieved Account details(Student) from fragment argument
        mStudent = (Student)getArguments().getSerializable(ACCOUNT_KEY);
        mDateString = getArguments().getString(DATE_KEY);


        mProfNameTextview = (TextView)view.findViewById(R.id.selectTime_ProfNameDisplayTextView);
        mProfNameTextview.setText(mProfessor.getName()+getString(R.string.schedule_for) +" "+mDateString);
        mBackButton = (Button)view.findViewById(R.id.selectTime_backeButton);
        mContinueButton = (Button)view.findViewById(R.id.selectTime_continueButton);

        mAdapter = new TimeAdapter(mTimeList);
        mTimeRecyclerView = (RecyclerView)view.findViewById(R.id.selectTime_TimeDisplayRecycler);
        mTimeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTimeRecyclerView.setAdapter(mAdapter);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectTimeUpdate.addSelectDateFrag(mProfessor);

            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSomethingSelected)
                    mSelectTimeUpdate.addComfirmFrag(mTimString);
                else
                    Toast.makeText(getActivity(), "TIME NOT SELECTED", Toast.LENGTH_LONG).show();
            }
        });

        //Send HTTP Request for professor schedule
        getProfessorSchedule();

        return view;
    }

    //Send HTTP Request for professor schedule
    public void getProfessorSchedule(){
        try {
            if(isConnected()){
                mJsonRequest.put("authorization", mStudent.getApiKey());
                mJsonRequest.put("profID", mProfessor.getId());
                mJsonRequest.put("date", mDateString);
                //Make HTTP connection to API and send Json user data
                mHttpHandler.processRequest(mJsonRequest, STUDENT_GET_PROF_SCHEDULE);

            }
            else{
                Snackbar.make(mSnackView, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        JSONArray schedule = mJsonResponse.getJSONArray("schedule");
        for(int i = 0; i<schedule.length(); i++){
            JSONObject timeJson = schedule.getJSONObject(i);
            boolean isbooked = timeJson.getBoolean("isBooked");
            String time = timeJson.getString("time");
            Time newTime = new Time(time, isbooked, false);
            mTimeList.add(newTime);
        }
        mAdapter.notifyDataSetChanged();
        //mUnCheckedTimeList = mTimeList;
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

    //Adapter for professor's schedule time recycle view
    public class TimeAdapter extends RecyclerView.Adapter<TimeHolder>  {

        private List<Time> adaptTime;

        public TimeAdapter(List<Time> timeList)
        {
            adaptTime = timeList;


        }

        @Override
        public TimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.professor_time_display, parent, false);
            return new TimeHolder(view);
        }

        @Override
        public void onBindViewHolder(TimeHolder holder, int position) {
            adaptTime.get(position).setPosition(position);
            holder.bind(adaptTime.get(position));
           // mAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return adaptTime.size();
        }
    }

    public class TimeHolder extends RecyclerView.ViewHolder  {

        private Time holderTime;
        private CheckBox mCheckbox;
        private CheckedTextView mTimeTextView;
        private CheckedTextView mIsAvailableTextView;
        private View mView;

        public TimeHolder(View itemView) {
            super(itemView);

            a ^= 1;
            //Change background color of itemView
            itemView.setBackgroundColor(colorss[a]);
            mCheckbox = (CheckBox)itemView.findViewById(R.id.timeDisplay_Checkbox);
            mTimeTextView = (CheckedTextView)itemView.findViewById(R.id.timeDisplay_timeTextiew);
            mIsAvailableTextView = (CheckedTextView)itemView.findViewById(R.id.timeDisplay_isAvailable);

            mTimeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectedTime = holderTime;
                    mTimString = holderTime.getTime();
                    mSomethingSelected = true;

                    mLastChecked = getAdapterPosition();
                    mAdapter.notifyDataSetChanged();
                }
            });

            mCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectedTime = holderTime;
                    mTimString = holderTime.getTime();
                    mSomethingSelected = true;

                    mLastChecked = getAdapterPosition();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        public void bind(Time time) {
            holderTime = time;
            mTimeTextView.setText(holderTime.getTime());
            boolean isBooked = holderTime.isBooked();
            if(isBooked){
                mIsAvailableTextView.setText("Booked");
                mIsAvailableTextView.setTextColor(Color.RED);
            }
            else
            {
                mIsAvailableTextView.setText("Available");
                mIsAvailableTextView.setTextColor(Color.GREEN);
            }
            mCheckbox.setChecked(holderTime.getPosition() == mLastChecked);

        }
    }


}
