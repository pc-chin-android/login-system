package com.pcchin.loginsys;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {
    // TODO: Room database
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Check if user is already logged in
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide support action bar & action bar as both of them need to be hidden
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void onBtnPressed(View view) {
        Intent intent;
        switch(view.getId()) {
            case R.id.login_about:
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.login_lost_password:
                intent = new Intent(this, ForgotPasswordActivity.class);
                break;
            case R.id.login_register:
                intent = new Intent(this, RegisterActivity.class);
                break;
            default:
                intent = new Intent();
                break;
        }
        startActivity(intent);
    }

    public void onSigninPressed(View view) {
        String username = String.valueOf(((TextView) findViewById(R.id.login_username_input)).getText());
        String password = String.valueOf(((TextView) findViewById(R.id.login_password_input)).getText());

        // Check username and password
        if (firstCheck(username, password)) {
            // TODO: Finish check
            Intent intent = new Intent(this, UserInfoActivity.class);
            intent.putExtra("Username", username);
            startActivity(intent);
        }
    }

    // Only used in onSigninPressed, separated for clarity
    private boolean firstCheck(@NotNull String username, String password) {
        boolean response = true;
        if (username.length() == 0) {
            TextView usernameError = findViewById(R.id.login_username_error);
            usernameError.setText(R.string.error_username_blank);
            response = false;
        }
        if (password.length() == 0) {
            TextView passwordError = findViewById(R.id.login_password_error);
            passwordError.setText(R.string.error_password_blank);
            response = false;
        }
        return response;
    }

    public void onExitPressed(View view) {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        // Press back to exit
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1500);

        }
    }
}
