package edu.rochester.meliorascheduler.Appointment;


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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.rochester.meliorascheduler.FirebaseHandlerServices.MyFirebaseInstanceIDService;
import edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread;
import edu.rochester.meliorascheduler.Models.Professor;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static com.google.firebase.crash.FirebaseCrash.setCrashCollectionEnabled;
import static edu.rochester.meliorascheduler.HttpHandler.HttpHandlerThread.STUDENT_SEARCH_PROF;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookAppointmentFragment extends Fragment {

    public interface BookAppointUpdate{
        public void addSelectDateFrag(Professor prof);
    }

    private BookAppointUpdate mBookAppointUpdate;
    private SearchView mSearchView;
    private Button mContinueButton;
    private RecyclerView mProfDisplayRecylcerView;
    private Student mStudent;

    private View mProfFullDataLayout;
    private TextView mProfNameTextview;
    private TextView mProfDeptTextView;
    private TextView mProfOfficeHrsTextView;
    private TextView mProfficeHrsDispTextView;

    private ProfessorAdapter mAdapter;
    private List<Professor>  mProfessorList = new ArrayList<>(10);

    private HttpHandlerThread mHttpHandler;
    //This is used in creating a snackbar
    private View mSnackView;

    //Json object to holding user data before sending to server
    JSONObject mJsonRequest = new JSONObject();
    //Json object to hold http response
    JSONObject mJsonResponse = new JSONObject();

    private Professor mSelectedProfessor;

    private FirebaseAnalytics mFirebaseAnalytics;
    private String mSearchTerm;


    public BookAppointmentFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mBookAppointUpdate = (BookAppointUpdate)context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mBookAppointUpdate = (BookAppointUpdate) activity;
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
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);
        mSnackView = view;

        setCrashCollectionEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        mStudent = (Student)getArguments().getSerializable(ACCOUNT_KEY);
        Log.d("TEST", mStudent.getApiKey());
        MyFirebaseInstanceIDService tokenRetriver = new MyFirebaseInstanceIDService();
        Log.d("TEST", tokenRetriver.getToken());

        mSearchView = (SearchView)view.findViewById(R.id.book_searchProfSearchView);
        mProfFullDataLayout = (View)view.findViewById(R.id.book_ProfLayoutView);
        mProfNameTextview = (TextView)view.findViewById(R.id.book_profNameTextView);
        mProfDeptTextView = (TextView)view.findViewById(R.id.book_profDeptTextView);
        mProfOfficeHrsTextView = (TextView)view.findViewById(R.id.book_profOfficeHrsTextView);
        mProfficeHrsDispTextView = (TextView)view.findViewById(R.id.book_profOfficeHrsDispTextView);

        mAdapter = new ProfessorAdapter(mProfessorList);
        mProfDisplayRecylcerView = (RecyclerView)view.findViewById(R.id.book_profDisplayRecycleView);
        mProfDisplayRecylcerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProfDisplayRecylcerView.setAdapter(mAdapter);
        mContinueButton = (Button)view.findViewById(R.id.book_continueButton);

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log Firebase search event
                //Bundle bundle = new Bundle();
                //bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, mSearchTerm);
                //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

                mBookAppointUpdate.addSelectDateFrag(mSelectedProfessor);
            }
        });



        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchTerm = s;
                mProfDisplayRecylcerView.setVisibility(View.VISIBLE);
                mProfFullDataLayout.setVisibility(View.GONE);
                mContinueButton.setVisibility(View.GONE);
                //Send query to database and update adapter for recycle view
                //Log.d("TEST", s);

                //Check whether user is connected to internet

                    try {
                        if(isConnected()){
                            mJsonRequest.put("authorization", mStudent.getApiKey());
                            mJsonRequest.put("name", s);
                            //Make HTTP connection to API and send Json user data
                            mHttpHandler.processRequest(mJsonRequest, STUDENT_SEARCH_PROF);
                        }
                        else{
                            Snackbar.make(mSnackView, "You are NOT connected to internet", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                return false;
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

                    boolean error = mJsonResponse.getBoolean("Found");
                    //Check for any error
                    if(!error){
                        mProfessorList.clear();
                        mAdapter.notifyDataSetChanged();
                        String message = mJsonResponse.getString("message");
                        //Display snackbar with error message
                        //makeSnack(mSnackView, message);
                    }
                    else{

                        mProfessorList.clear();
                        mAdapter.notifyDataSetChanged();
                        updateAdapterArray();
                        //Notify adapter of data set change

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mHttpHandler.start();
        mHttpHandler.getLooper();
    }

    //This updates Adapter array with search result
    public void updateAdapterArray() throws JSONException {
        //mProfessorList.clear();
        JSONArray profList = mJsonResponse.getJSONArray("message");

        String bt = Integer.toString(profList.length());
        //mProfessorList.clear();
        for(int i = 0; i<profList.length(); i++){

            JSONObject profOBJ = profList.getJSONObject(i);
            //Log.d("TEST", profOBJ.getString("name"));
            Professor proff = new Professor();
            proff.setId(profOBJ.getInt("id"));
            proff.setName(profOBJ.getString("name"));
            proff.setEmail(profOBJ.getString("email"));
            proff.setDepartment(profOBJ.getString("department"));
            proff.setOfficeLocation(profOBJ.getString("officLoc"));

            JSONObject offhrs = profOBJ.getJSONObject("officeHrs");
            proff.setOfficeHrs(getOfficeHrs(offhrs));

            JSONArray keys = offhrs.names();
            String[] resultingArray = keys.join(",").split(",");
            proff.setOfficeDays(resultingArray);
            mProfessorList.add(i,proff);

        }
        mAdapter.notifyDataSetChanged();
    }

    //This is a helper function to retrieve office hours from json array
    public String getOfficeHrs(JSONObject prof){
        StringBuilder officeHrs = new StringBuilder();
        JSONArray keys = prof.names();
        try {
            //loop through daily office hours
            for(int i = 0; i<keys.length(); i++){

                String dayStr = keys.getString(i);
                String day = getDayOfWeek(Integer.parseInt(dayStr));
                officeHrs.append(day).append(": ");
                //Retrieve a single day containing array of office hrs
                JSONArray r = prof.getJSONArray(dayStr);
                for(int j = 0; j<r.length(); j++){
                    officeHrs.append(r.getString(j));
                    officeHrs.append(" ");
                }
                //Append new line
                officeHrs.append(System.lineSeparator());
                //Log.d("TEST", prof.getString(dayStr));
            }

        } catch (JSONException | ParseException e) {
                e.printStackTrace();
        }

        return officeHrs.toString();
    }

    //This converts DayOfWeek(int) to DayOfWeek(String)
    public String getDayOfWeek(int day) throws ParseException {
        String[] dayStr = new String[8];
        dayStr[1] = "Sun";
        dayStr[2] = "Mon";
        dayStr[3] = "Tue";
        dayStr[4] = "Wed";
        dayStr[5] = "Thu";
        dayStr[6] = "Fri";
        dayStr[7] = "Sat";

        return dayStr[day];
        //Log.d("TEST", Integer.toString(dayOfWeek));
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




    //Adapter for professor recycle view
    public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorHolder>  {

        private List<Professor> adapProfessors;

        public ProfessorAdapter(List<Professor> professors)
        {
            adapProfessors = professors;
        }

        @Override
        public ProfessorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.professor_display, parent, false);
            return new ProfessorHolder(view);
        }

        @Override
        public void onBindViewHolder(ProfessorHolder holder, int position) {
            holder.bind(adapProfessors.get(position));

        }

        @Override
        public int getItemCount() {
            return adapProfessors.size();
        }
    }

    public class ProfessorHolder extends RecyclerView.ViewHolder  {

        private Professor holdProfessor;
        private TextView mProfNameDisplay;
        private View mView;

        public ProfessorHolder(View profView) {
            super(profView);

            mView = profView;

            mProfNameDisplay = (TextView)profView.findViewById(R.id.profDisplay_nameTextView);
            mProfNameDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProfFullDataLayout.setVisibility(View.VISIBLE);
                    mContinueButton.setVisibility(View.VISIBLE);
                    mProfDisplayRecylcerView.setVisibility(View.GONE);

                    mProfNameTextview.setText(holdProfessor.getName());
                    mProfDeptTextView.setText(holdProfessor.getDepartment());
                    mProfOfficeHrsTextView.setText(R.string.officehrs);
                    mProfficeHrsDispTextView.setText(holdProfessor.getOfficeHrs());

                    mSelectedProfessor = holdProfessor;

                    //mView.setBackgroundColor(Color.GRAY);

                }
            });
        }

        public void bind(Professor professor) {
            holdProfessor = professor;
            mProfNameDisplay.setText(holdProfessor.getName());

        }
    }

}
