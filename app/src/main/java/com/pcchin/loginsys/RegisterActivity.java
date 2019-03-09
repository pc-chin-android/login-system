package com.pcchin.loginsys;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pcchin.loginsys.database.UserDatabase;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText usernameInput = findViewById(R.id.register_username_input);
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editedText = s.toString();
                UserDatabase database = Room.databaseBuilder(getApplicationContext(),
                        UserDatabase.class, "userAccount").allowMainThreadQueries().build();
                TextView usernameError = findViewById(R.id.register_username_error);
                if (database.userDao().searchByUsername(editedText) != null) {
                    usernameError.setText(R.string.error_username_taken);
                } else {
                    usernameError.setText(R.string.blank);
                }
            }
        });

        TextView birthdayDisplay = findViewById(R.id.register_birthday_current);
        birthdayDisplay.setText(String.format(Locale.ENGLISH, "%s%s",
                getString(R.string.current_birthday), new SimpleDateFormat("dd/MM/yyyy",
                        Locale.ENGLISH).format(new Date().getTime())));
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

    public void onRegisterPressed(View view) {
        // TODO: Complete
    }

    public void onBirthdayPressed(View view) {
        // TODO: Complete
    }

    // Carry forward function
    public void onCancelPressed(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
