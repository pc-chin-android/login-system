package com.pcchin.loginsys;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class UserEditActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private ProgressDialog waitingDialog;
    private int EDIT_PICK_IMAGE = 212;
    private UserDatabase database;
    private UserAccount currentUser;
    private String username;
    private String birthday;
    private Bitmap profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getStringExtra("username");
        setContentView(R.layout.activity_user_edit);

        // Take values from user
        sharedPref = getSharedPreferences("com.pcchin.loginsys", MODE_PRIVATE);
        database = Room.databaseBuilder(getApplicationContext(),
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        currentUser = database.userDao().searchByUsername(username);
        ((EditText) findViewById(R.id.edit_username_input)).setText(currentUser.username);
        ((EditText) findViewById(R.id.edit_firstname_input)).setText(currentUser.firstName);
        ((EditText) findViewById(R.id.edit_lastname_input)).setText(currentUser.lastName);
        birthday = currentUser.birthday;
        ((TextView) findViewById(R.id.edit_birthday_current)).setText(String.format(Locale.ENGLISH,
                "%s%s", getString(R.string.current_birthday), birthday));
        profileImg = GeneralFunctions.getBitmap(currentUser.photo);
        ((ImageView) findViewById(R.id.edit_photo_display)).setImageBitmap(profileImg);
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
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    // Carry forward function
    public void onEditCancelPressed(View view) {
        this.onBackPressed();
    }

    public void onEditPhotoPressed(View view) {
        // Open gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, EDIT_PICK_IMAGE);
    }

    public void onEditBirthdayPressed(View view) {
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

    public void onEditRegisterPressed(View view) {
        // Clears out all errors
        ((TextView) findViewById(R.id.edit_password1_error)).setText(R.string.blank);
        ((TextView) findViewById(R.id.edit_password2_error)).setText(R.string.error_password_new_blank);

        // Values will only update when password check met
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (passwordCheck()) {
                    // Update values
                    currentUser.birthday = birthday;
                    currentUser.firstName = ((EditText) findViewById(R.id.edit_firstname_input))
                            .getText().toString();
                    currentUser.lastName = ((EditText) findViewById(R.id.edit_lastname_input))
                            .getText().toString();

                    // Update code if not blank
                    String codeInput = ((EditText) findViewById(R.id.edit_code_input)).getText().toString()
                            .replaceAll("\\s++", "");
                    if (codeInput.length() > 0) {
                        currentUser.codehash = GeneralFunctions.passwordHash(codeInput, currentUser.salt,
                                currentUser.creationDate);
                    }

                    // Update password if not blank
                    String passwordInput = ((EditText) findViewById(R.id.edit_password2_input)).getText().toString();
                    String guid = sharedPref.getString("guidString", "");
                    if (guid != null && passwordInput.length() > 0) {
                        // Password is already checked at the start and guid is never null,
                        // but it is implemented to prevent NullPointerException
                        currentUser.passhash = GeneralFunctions.passwordHash(passwordInput,
                                currentUser.salt, guid);
                    }

                    // Set up photo
                    String photoUrl = getFilesDir().getAbsolutePath() + "/" + Integer.toString(currentUser.userId) + ".jpg";
                    GeneralFunctions.storeBitmap(profileImg, photoUrl);

                    // Check if admin code matches
                    String adminCode = sharedPref.getString("adminCode", "");
                    String adminInput = null;
                    if (guid != null) {
                        adminInput = GeneralFunctions.passwordHash(
                                ((EditText) findViewById(R.id.edit_admin)).getText().toString(),
                                guid, guid);
                    }
                    if (adminInput != null && Objects.equals(adminCode, adminInput)) {
                        currentUser.isAdmin = true;
                    }

                    // Update value
                    database.userDao().update(currentUser);

                    onBackPressed();
                }
                waitingDialog.dismiss();
            }
        }).start();

        // Show waiting spinner
        waitingDialog = new ProgressDialog(this);
        waitingDialog.setIndeterminate(true);
        waitingDialog.setMessage(getString(R.string.saving_changes));
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitingDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_PICK_IMAGE) {
            Uri imageUri = data.getData();
            try {
                profileImg = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            profileImg = Bitmap.createScaledBitmap(profileImg, 256, 256, false);
            ImageView profileView = findViewById(R.id.edit_photo_display);
            profileView.setImageBitmap(profileImg);
        }
    }

    private boolean passwordCheck() {
        EditText password1Input = findViewById(R.id.edit_password1_input);
        EditText password2Input = findViewById(R.id.edit_password2_input);
        TextView password1Error = findViewById(R.id.edit_password1_error);
        TextView password2Error = findViewById(R.id.edit_password2_error);

        boolean response = true;

        if (password2Input.getText().toString().length() == 0) {
            // Earlier return to save processing time
            return true;
        }

        if (password2Input.getText().toString().length() < 8) {
            password2Error.setText(R.string.error_password_short);
            response = false;
        } else if (password2Input.getText().toString().matches("\\A\\p{ASCII}*\\z")) {
            password2Error.setText(R.string.error_password_utf);
        }

        String guid = sharedPref.getString("guidString", "");
        if (guid == null || !GeneralFunctions.passwordHash(password1Input.getText().toString(),
                currentUser.salt, guid).equals(currentUser.passhash)) {
            // GUID is never null, but implemented to prevent NullPointerException
            password1Error.setText(R.string.error_password_incorrect);
            response = false;
        }

        return response;
    }
}
