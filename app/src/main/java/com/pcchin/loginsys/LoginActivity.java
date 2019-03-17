package com.pcchin.loginsys;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.loginsys.database.UserDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SharedPreferences.Editor editor;
    private String guidString;
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("com.pcchin.loginsys", MODE_PRIVATE);

        // Get unique GUID
        guidString = sharedPref.getString("guidString", "");
        if (guidString == null || guidString.length() == 0) {
            // Set up GUID
            guidString = UUID.randomUUID().toString();
            editor = sharedPref.edit();
            editor.putString("guidString", guidString);
            editor.apply();
        }

        // Set up admin code
        String adminCode = sharedPref.getString("adminCode", "");
        if (adminCode != null && adminCode.length() == 0) {
            // Admin code will never be null, but this prevents a NullPointerException
            // Error suppressed as dialogs do not have parents
            @SuppressLint("InflateParams") final View adminCodeView = getLayoutInflater()
                    .inflate(R.layout.popup_admin, null);
            final AlertDialog adminCodeDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .setTitle(R.string.select_admin_code)
                    .setView(adminCodeView)
                    .setPositiveButton(R.string.forward, null).create();
            adminCodeDialog.show();

            // Changes listener after shown
            adminCodeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText adminCodeInput = adminCodeView.findViewById(R.id.popup_admin_input);
                            String output = adminCodeInput.getText().toString();
                            if (output.replace("\\s+", "").length() > 0) {
                                // Check if any text is in input
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("adminCode", GeneralFunctions
                                        .passwordHash(output, guidString, guidString));
                                editor.apply();
                                adminCodeDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.error_admin_blank),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        }

        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(this, UserInfoActivity.class);
            intent.putExtra("username", sharedPref.getString("currentUser", ""));
            startActivity(intent);
        }
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
        final String username = String.valueOf(((TextView) findViewById(R.id.login_username_input))
                .getText()).toLowerCase();
        final String password = String.valueOf(((TextView) findViewById(R.id.login_password_input))
                .getText());

        // Reset error messages
        TextView usernameError = findViewById(R.id.login_username_error);
        TextView passwordError = findViewById(R.id.login_password_error);
        usernameError.setText(R.string.blank);
        passwordError.setText(R.string.blank);

        Thread loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Check username and password
                if (firstCheck(username, password) && secondCheck(username, password)) {
                    // Sets isLoggedIn to true
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("currentUser", username);
                    editor.apply();

                    // Starts activity
                    Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
                onLoginThreadComplete();
            }
        });
        loginThread.start();

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    // Only used in onSigninPressed, separated for clarity
    private boolean firstCheck(@NotNull String username, String password) {
        // Check if both fields are filled
        boolean response = true;
        if (username.replaceAll("\\s+", "").length() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView usernameError = findViewById(R.id.login_username_error);
                    usernameError.setText(R.string.error_username_blank);
                }
            });
            response = false;
        }
        if (password.length() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView passwordError = findViewById(R.id.login_password_error);
                    passwordError.setText(R.string.error_password_blank);
                }
            });
            response = false;
        }
        return response;
    }

    // Only used in onSigninPressed, separated for clarity
    private boolean secondCheck(String username, String password) {
        UserDatabase database = Room.databaseBuilder(this,
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        boolean response = true;
        if (database.userDao().searchByUsername(username) == null) {
            // Check if user exists
            response = false;
        } else {
            // Check if password passwordHash matches
            // Separated for clarity and to prevent NullPointerException
            String salt = database.userDao().searchByUsername(username).salt;
            String currentPass = database.userDao().searchByUsername(username).passhash;
            if (!GeneralFunctions.passwordHash(password, salt, guidString).equals(currentPass)) {
                response = false;
            }
        }
        if (!response) {
            // Display error message
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView usernameError = findViewById(R.id.login_username_error);
                    usernameError.setText(R.string.error_incorrect);
                }
            });
        }
        return response;
    }

    private void onLoginThreadComplete() {
        progressDialog.dismiss();
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
