package edu.rochester.meliorascheduler.ForgotPassword;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.rochester.meliorascheduler.R;

public class ForgotPassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        ForgotPassFragment frag = new ForgotPassFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.ForgotPass_LayoutFrag, frag)
                .commit();
    }
}
