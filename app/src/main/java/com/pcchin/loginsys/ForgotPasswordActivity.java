package com.pcchin.loginsys;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Carry forward function
    public void onForgotCancelPressed(View view) {
        this.onBackPressed();
    }

    public void onForgotNextPressed(View view) {
        TextView usernameError = findViewById(R.id.forgot_username_error);
        TextView codeError = findViewById(R.id.forgot_code_error);
        usernameError.setText(R.string.blank);
        codeError.setText(R.string.blank);

        // Check if user exists
        String username = ((EditText) findViewById(R.id.forgot_username_input)).getText().toString();
        String code = ((EditText) findViewById(R.id.forgot_code_input)).getText().toString();

        UserDatabase database = Room.databaseBuilder(this,
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        if (database.userDao().searchByUsername(username) == null) {
            usernameError.setText(R.string.error_username_missing);
        } else {
            UserAccount currentUser = database.userDao().searchByUsername(username);
            // Check if code matches
            if (GeneralFunctions.passwordHash(code, currentUser.salt, currentUser.creationDate)
                    .equals(currentUser.codehash)) {
                // Reset password
                Intent intent = new Intent(this, NewPasswordActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                codeError.setText(R.string.error_code_incorrect);
            }
        }
    }
}
