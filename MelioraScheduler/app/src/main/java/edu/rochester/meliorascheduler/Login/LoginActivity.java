package edu.rochester.meliorascheduler.Login;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import edu.rochester.meliorascheduler.CreateAccount.CreateAccountActivity;
import edu.rochester.meliorascheduler.CreateAccount.CreateAccountFragment;
import edu.rochester.meliorascheduler.ForgotPassword.ForgotPassActivity;
import edu.rochester.meliorascheduler.Home.HomeActivity;
import edu.rochester.meliorascheduler.Models.Student;
import edu.rochester.meliorascheduler.R;

import static edu.rochester.meliorascheduler.Login.LoginFragment.SHAREDPREF_APIKEY;
import static edu.rochester.meliorascheduler.Login.LoginFragment.SHAREDPREF_STATUSKEY;


public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginFragUpdate {

    public static final String ACCOUNT_KEY = "accountKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginFragment frag = new LoginFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.login_Layout, frag)
                .commit();
    }

    @Override
    public void loginSuccess(Student account) {

        //Toast.makeText(this, "LOGIN WAS SUCCESSFULL!!", Toast.LENGTH_LONG).show();
        //store username in sharedPreference. Used to login user on next access
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SHAREDPREF_STATUSKEY, "LoggedIn")
                .putString(SHAREDPREF_APIKEY, account.getApiKey())
                .apply();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra(ACCOUNT_KEY, account);
        startActivity(intent);
        finish();

    }

    @Override
    public void addForgotPassFrag() {
        Intent intent = new Intent(LoginActivity.this, ForgotPassActivity.class);
        startActivity(intent);
    }

    @Override
    public void addCreateAccountFrag() {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }
}
