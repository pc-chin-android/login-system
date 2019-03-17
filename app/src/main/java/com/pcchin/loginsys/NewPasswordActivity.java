package com.pcchin.loginsys;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

public class NewPasswordActivity extends AppCompatActivity {
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        username = getIntent().getStringExtra("username");
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
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    // Carry forward function
    public void onNewCancelPressed(View view) {
        this.onBackPressed();
    }

    public void onNewNextPressed(View view) {
        String guid = getSharedPreferences("com.pcchin.loginsys", MODE_PRIVATE)
                .getString("guidString", "");
        if (checkRequirements() && guid != null) {
            // GUID is never null, but implemented to prevent NullPointerException
            // Update password
            UserDatabase database = Room.databaseBuilder(getApplicationContext(),
                    UserDatabase.class, "userAccount").allowMainThreadQueries().build();
            UserAccount currentUser = database.userDao().searchByUsername(username);
            currentUser.passhash = GeneralFunctions.passwordHash(
                    ((EditText) findViewById(R.id.new_password1_input)).getText()
                    .toString(), currentUser.salt, guid);

            // Return to login
            Toast.makeText(this, getString(R.string.password_updated), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private boolean checkRequirements() {
        String password1 = ((EditText) findViewById(R.id.new_password1_input)).getText().toString();
        String password2 = ((EditText) findViewById(R.id.new_password2_input)).getText().toString();
        TextView password1Error = findViewById(R.id.new_password1_error);
        TextView password2Error = findViewById(R.id.new_password2_error);

        password1Error.setText(R.string.blank);
        password2Error.setText(R.string.blank);

        // Check for blank fields
        if (password1.length() == 0) {
            password1Error.setText(getString(R.string.error_password_blank));
            return false;
        } else if (password1.length() < 8) {
            password1Error.setText(getString(R.string.error_password_short));
            return false;
        } else if (! password1.equals(password2)) {
            password2Error.setText(R.string.error_password_differ);
            return false;
        }

        return true;
    }
}
