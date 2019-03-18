package com.pcchin.loginsys;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private ProgressDialog waitingDialog;
    private int PICK_IMAGE = 121;
    private String birthday = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).
            format(new Date());
    private Bitmap profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Looking out for input
        EditText usernameInput = findViewById(R.id.register_username_input);
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView usernameError = findViewById(R.id.register_username_error);
                usernameError.setText(R.string.blank);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String editedText = s.toString().replaceAll("\\s+", "").toLowerCase();
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

        EditText passwordInput1 = findViewById(R.id.register_password1_input);
        EditText passwordInput2 = findViewById(R.id.register_password2_input);
        TextWatcher passwordTextListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView password1Error = findViewById(R.id.register_password1_error);
                TextView password2Error = findViewById(R.id.register_password2_error);
                password1Error.setText(R.string.blank);
                password2Error.setText(R.string.blank);
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordCheck();
            }
        };
        passwordInput1.addTextChangedListener(passwordTextListener);
        passwordInput2.addTextChangedListener(passwordTextListener);

        // Set birthday to today
        TextView birthdayDisplay = findViewById(R.id.register_birthday_current);
        birthdayDisplay.setText(String.format(Locale.ENGLISH, "%s%s",
                getString(R.string.current_birthday), birthday));

        // Set default profile image
        profileImg = BitmapFactory.decodeResource(getResources(), R.drawable.user);
        ImageView userImg = findViewById(R.id.register_photo_display);
        userImg.setImageBitmap(profileImg);
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
        // Clears out all errors
        TextView[] errorList = {findViewById(R.id.register_username_error),
                findViewById(R.id.register_password1_error), findViewById(R.id.register_password2_error),
                findViewById(R.id.register_code_error), findViewById(R.id.register_tnc_error)};
        for (TextView t: errorList) {
            t.setText(R.string.blank);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkRequirements()) {
                    Random random = new Random();
                    final UserDatabase database = Room.databaseBuilder(getApplicationContext(),
                            UserDatabase.class, "userAccount").allowMainThreadQueries().build();
                    boolean gettingId = true;
                    int uid = 0;
                    while (gettingId) {
                        // Keep trying until unique ID is generated
                        uid = Math.abs(random.nextInt());
                        if (database.userDao().searchById(uid) == null) {
                            gettingId = false;
                        }
                    }

                    // Get values of user
                    final String guid = getSharedPreferences("com.pcchin.loginsys", MODE_PRIVATE).
                            getString("guidString", "");
                    if (guid != null) {
                        final String username = ((EditText) findViewById(R.id.register_username_input)).
                                getText().toString().toLowerCase();

                        String password = ((EditText) findViewById(R.id.register_password1_input)).
                                getText().toString();
                        String code = ((EditText) findViewById(R.id.register_code_input)).getText().toString();
                        String firstName = ((EditText) findViewById(R.id.register_firstname_input)).
                                getText().toString();
                        String lastName = ((EditText) findViewById(R.id.register_lastname_input)).
                                getText().toString();
                        String salt = RandomStringUtils.randomAscii(40, 60);
                        String creationDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).
                                format(new Date());
                        String passHash = GeneralFunctions.passwordHash(password, salt, guid);
                        String codeHash = GeneralFunctions.passwordHash(code, salt, creationDate);
                        // Set up photo
                        String photoUrl = getFilesDir().getAbsolutePath() + "/" + Integer.toString(uid) + ".jpg";
                        GeneralFunctions.storeBitmap(profileImg, photoUrl);
                        // Set up new user
                        UserAccount user = new UserAccount(uid, username, creationDate, firstName, lastName,
                                salt, passHash, codeHash, birthday, photoUrl);
                        database.userDao().insert(user);

                        // Set isLoggedIn to true
                        SharedPreferences.Editor editor = getSharedPreferences("com.pcchin.loginsys", MODE_PRIVATE).edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("currentUser", username);
                        editor.apply();

                        // Dismiss popup if present
                        if (waitingDialog != null && waitingDialog.isShowing()) {
                            waitingDialog.dismiss();
                        }

                        // Go to user info
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                }
                waitingDialog.dismiss();
            }
        }).start();

        // Show waiting spinner
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setIndeterminate(true);
        waitingDialog.setMessage(getString(R.string.creating_acc));
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitingDialog.show();
    }

    // Only used in onRegisterPressed(), separated for clarity
    private boolean checkRequirements() {
        boolean response = true;
        // Check if any of the required fields are blank
        EditText usernameInput = findViewById(R.id.register_username_input);
        if (usernameInput.getText().toString().replaceAll("\\s+", "").length() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView usernameError = findViewById(R.id.register_username_error);
                    usernameError.setText(R.string.error_username_blank);
                }
            });
            response = false;
        }

        if (! passwordCheck()) {
            response = false;
        }

        EditText codeInput = findViewById(R.id.register_code_input);
        if (codeInput.getText().toString().replaceAll("\\s+", "").length() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView codeError = findViewById(R.id.register_code_error);
                    codeError.setText(R.string.error_code_blank);
                }
            });
            response = false;
        }

        CheckBox tncCheck = findViewById(R.id.register_tnc);
        if (! tncCheck.isChecked()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tncError = findViewById(R.id.register_tnc_error);
                    tncError.setText(R.string.error_checkbox_fail);
                }
            });
            response = false;
        }

        return response;
    }

    // Will be run from UI Thread
    private boolean passwordCheck() {
        EditText password1Input = findViewById(R.id.register_password1_input);
        EditText password2Input = findViewById(R.id.register_password2_input);
        TextView password1Error = findViewById(R.id.register_password1_error);
        TextView password2Error = findViewById(R.id.register_password2_error);

        boolean response = true;

        // Check for blank fields
        if (password1Input.getText().toString().length() == 0) {
            password1Error.setText(R.string.error_password_blank);
            response = false;
        }

        if (password1Input.getText().toString().length() < 8) {
            // Length requirement
            password1Error.setText(R.string.error_password_short);
            response = false;
        } else if (! password1Input.getText().toString().matches("\\A\\p{ASCII}*\\z")) {
            // Password can only contain ASCII characters
            password1Error.setText(R.string.error_password_utf);
            response = false;
        }

        if ((password1Input.getText().toString().length() != 0 && password2Input.getText().toString().length() != 0)
                && (! password1Input.getText().toString().equals(password2Input.getText().toString()))) {
            // Passwords do not match
            password2Error.setText(R.string.error_password_differ);
            response = false;
        }

        return response;
    }

    public void onPhotoPressed(View view) {
        // Open gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    public void onBirthdayPressed(View view) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birthday = String.format(Locale.ENGLISH, "%d/%d/%d", dayOfMonth, month, year);
                TextView birthdayView = findViewById(R.id.register_birthday_current);
                birthdayView.setText(String.format("%s%s", getString(R.string.current_birthday), birthday));
            }
        };
        DatePickerDialog datePicker = new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            try {
                profileImg = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            profileImg = Bitmap.createScaledBitmap(profileImg, 256, 256, false);
            ImageView profileView = findViewById(R.id.register_photo_display);
            profileView.setImageBitmap(profileImg);
        }
    }
}
