package edu.rochester.meliorascheduler.CreateAccount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.rochester.meliorascheduler.Home.HomeActivity;
import edu.rochester.meliorascheduler.Login.LoginActivity;
import edu.rochester.meliorascheduler.R;

public class CreateAccountActivity extends AppCompatActivity implements CreateAccountFragment.CreateAccountUpdate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        CreateAccountFragment frag = new CreateAccountFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.CreateAccount_LayoutFrag,frag)
                .commit();
    }

    @Override
    public void accountCreated() {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
