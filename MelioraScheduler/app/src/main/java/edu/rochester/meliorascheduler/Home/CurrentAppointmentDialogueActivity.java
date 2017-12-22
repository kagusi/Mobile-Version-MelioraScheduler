package edu.rochester.meliorascheduler.Home;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.rochester.meliorascheduler.Models.Appointment;
import edu.rochester.meliorascheduler.R;

public class CurrentAppointmentDialogueActivity extends Activity {

    //For dialogue
    private TextView mReasonTexView;
    private TextView pop_ProfNameTextView;
    private TextView pop_OfficLoc;
    private TextView pop_Date;
    private TextView pop_Time;
    private TextView pop_Reason;
    private Button mOKButton;

    private Appointment mAppointment;
    public static final String APPOINTMENT_KEY = "appointment_key";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_popout);

        mAppointment = (Appointment)getIntent().getSerializableExtra(APPOINTMENT_KEY);

        //For dialogue
        pop_ProfNameTextView = (TextView)findViewById(R.id.appointPop_ProfNameTextView);
        pop_OfficLoc = (TextView)findViewById(R.id.appointPop_OfficeLocTextView);
        pop_Date = (TextView)findViewById(R.id.appointPop_ProfDateTextView);
        pop_Time = (TextView)findViewById(R.id.appointPop_ProfTimeTextView);
        pop_Reason = (TextView)findViewById(R.id.appointPop_ProfReasonTextView);
        mOKButton = (Button)findViewById(R.id.appoint_pop_okButton);
        mReasonTexView = (TextView)findViewById(R.id.appointPop_ReasonTextView);

        pop_ProfNameTextView.setText(mAppointment.getProfName());
        pop_OfficLoc.setText(mAppointment.getProfOfficeLoc());
        pop_Date.setText(getStringDate(mAppointment.getAppointmentDate()));
        pop_Time.setText(mAppointment.getAppointmentTime());
        if(mAppointment.isCancelled()){
            mReasonTexView.setText("Reason for cancellation");
            pop_Reason.setText(mAppointment.getReasonForCancel());
        }
        else{
            pop_Reason.setText(mAppointment.getReasoForAppointment());
        }


        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //Convert date to full date string (eg Tue Sept 24, 20017)
    public String getStringDate(String dt){
        String dateString = "";
        DateFormat format = new SimpleDateFormat("yyyy/M/d", Locale.US);
        try {
            Date date = format.parse(dt);
            DateFormat outputFormatter = new SimpleDateFormat("E MMM d, yyyy", Locale.US);
            dateString = outputFormatter.format(date);
            return dateString;
            //Log.d("TEST", d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }
}
