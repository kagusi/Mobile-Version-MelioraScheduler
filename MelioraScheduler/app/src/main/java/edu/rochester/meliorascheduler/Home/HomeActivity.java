package edu.rochester.meliorascheduler.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.rochester.meliorascheduler.Appointment.AppointmentHistoryFragment;
import edu.rochester.meliorascheduler.Appointment.BookAppointment.ComfirmFragment;
import edu.rochester.meliorascheduler.Appointment.BookAppointment.SelectDateFragment;
import edu.rochester.meliorascheduler.Appointment.BookAppointment.SelectTimeFragment;
import edu.rochester.meliorascheduler.Appointment.BookAppointmentFragment;
import edu.rochester.meliorascheduler.Login.LoginActivity;
import edu.rochester.meliorascheduler.Models.Appointment;
import edu.rochester.meliorascheduler.FirebaseHandlerServices.MyFirebaseInstanceIDService;
import edu.rochester.meliorascheduler.Models.Professor;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static com.google.firebase.crash.FirebaseCrash.setCrashCollectionEnabled;
import static edu.rochester.meliorascheduler.Home.CurrentAppointmentDialogueActivity.APPOINTMENT_KEY;
import static edu.rochester.meliorascheduler.Login.LoginActivity.ACCOUNT_KEY;
import static edu.rochester.meliorascheduler.Login.LoginFragment.SHAREDPREF_STATUSKEY;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomeFragment.HomeFragUpdate,
        BookAppointmentFragment.BookAppointUpdate, SelectDateFragment.SelectDateUpdate,
        SelectTimeFragment.SelectTimeUpdate, ComfirmFragment.ComfirmFragUpdate {

    private Student mStudent;
    //Selected professor for book appointment
    private Professor mProfessor;
    private String mDate;
    private String mTime;

    //For navigation header
    private TextView mNavProfileNameTextView;
    private TextView mNavDateTextView;

    public static final String PROFNAME_KEY = "profName_key";
    public static final String DATE_KEY = "date_key";
    public static final String PROF_KEY = "prof_key";
    public static final String TIME_KEY = "appointment_time";
    public static final int SHARE_INVITE_KEY = 850;

    private String mDeviceToken;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setCrashCollectionEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mStudent = (Student)getIntent().getSerializableExtra(ACCOUNT_KEY);

        //Retrieve api key for this device
        MyFirebaseInstanceIDService tokenRetriver = new MyFirebaseInstanceIDService();
        mDeviceToken = tokenRetriver.getToken();

        Vibrator v = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavProfileNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navHeader_ProfileNameTextView);
        mNavDateTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navHeader_DatetextView);

        //Update Nav header
        updateDrawerHeader();

        //Add home fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        HomeFragment frag = new HomeFragment();
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();

    }


    public void updateDrawerHeader()
    {
        mNavProfileNameTextView.setText(mStudent.getName());
        DateFormat dateformat = new SimpleDateFormat("MM/dd/yy");
        Date currentDate = new Date();
        mNavDateTextView.setText(dateformat.format(currentDate));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ACCOUNT_KEY, mStudent);
            HomeFragment frag = new HomeFragment();
            frag.setArguments(bundle);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Content_fragLayout, frag)
                    .commit();

        } else if (id == R.id.nav_history) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ACCOUNT_KEY, mStudent);
            AppointmentHistoryFragment frag = new AppointmentHistoryFragment();
            frag.setArguments(bundle);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Content_fragLayout, frag)
                    .commit();

        } else if (id == R.id.nav_book_appointment) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ACCOUNT_KEY, mStudent);
            BookAppointmentFragment frag = new BookAppointmentFragment();
            frag.setArguments(bundle);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Content_fragLayout, frag)
                    .commit();

        }
        else if (id == R.id.nav_logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit()
                    .remove(SHAREDPREF_STATUSKEY)
                    .putString(SHAREDPREF_STATUSKEY, "LoggedOut")
                    .apply();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_share) {
            //Log Firebase share event
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Shared App");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
            Intent intent = new AppInviteInvitation.IntentBuilder("Send Invitation for "+R.string.app_name)
                    .setMessage(getString(R.string.invitation_message))
                    //.setEmailSubject("Check out Meliora Scheduler")
                    //.setEmailHtmlContent(getString(R.string.invitation_message))
                    //.setCustomImage(Uri.parse(R.mipmap.))
                    .setCallToActionText(getString(R.string.app_name))
                    .build();
            startActivityForResult(intent, SHARE_INVITE_KEY);

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void addBookAppointment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        BookAppointmentFragment frag = new BookAppointmentFragment();
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();
    }

    @Override
    public void addAppointmentDialogue(Appointment appointment) {
        Intent intent = new Intent(HomeActivity.this, CurrentAppointmentDialogueActivity.class);
        intent.putExtra(APPOINTMENT_KEY, appointment);
        startActivity(intent);
        //Intent intent = new Intent(getActivity(), CurrentAppointmentDialogueActivity.class);
        //intent.putExtra(APPOINTMENT_KEY, holderAppointment);
        //startActivity(intent);
    }

    @Override
    public void addComfirmFrag(String time, String date) {
        ComfirmFragment frag = new ComfirmFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROF_KEY, mProfessor);
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        bundle.putSerializable(TIME_KEY, time);
        bundle.putSerializable(DATE_KEY, date);
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();
    }

    @Override
    public void addSelectDateFrag(Professor prof) {
        mProfessor = prof;
        SelectDateFragment frag = new SelectDateFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROF_KEY, mProfessor);
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();
    }

    @Override
    public void addComfirmFrag(String time) {

    }

    @Override
    public void addSelectDateFrag() {
        SelectDateFragment frag = new SelectDateFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROF_KEY, mProfessor);
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();
    }

    /*
    @Override
    public void addSelectTimeFrag(String date) {
        mDate = date;
        SelectTimeFragment frag = new SelectTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROF_KEY, mProfessor);
        bundle.putString(DATE_KEY, mDate);
        bundle.putSerializable(ACCOUNT_KEY, mStudent);
        frag.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Content_fragLayout, frag)
                .commit();
    }
    */




}
