package edu.rochester.meliorascheduler.SplashScreen;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import edu.rochester.meliorascheduler.Home.HomeActivity;
import edu.rochester.meliorascheduler.Login.LoginActivity;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.Login.LoginFragment.SHAREDPREF_APIKEY;
import static edu.rochester.meliorascheduler.Login.LoginFragment.SHAREDPREF_STATUSKEY;

public class SplashScreenActivity extends AppCompatActivity implements SplashScreenFragment.SplashScreenUpdate {

    public static final String ACCOUNT_KEY = "accountKey";
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);

        }
        else{
            Fragment frag = new SplashScreenFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.splash_Layout, frag)
                    .commit();
        }


    }

    @Override
    public void addLoginPage() {
        //Log.d("TEST", "Hello");
        Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void loginSuccess(Student account) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SHAREDPREF_STATUSKEY, "LoggedIn")
                .putString(SHAREDPREF_APIKEY, account.getApiKey())
                .apply();

        Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
        intent.putExtra(ACCOUNT_KEY, account);
        startActivity(intent);
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Fragment frag = new SplashScreenFragment();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.splash_Layout, frag)
                            .commit();

                } else {

                    finish();
                }
            }

        }
    }
}
