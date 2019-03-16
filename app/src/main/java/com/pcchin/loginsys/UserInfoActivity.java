package com.pcchin.loginsys;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

import java.util.Locale;

import static android.view.View.INVISIBLE;

public class UserInfoActivity extends AppCompatActivity {
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        // FIXME: savedInstanceState is null
        username = savedInstanceState.getString("username", "");
        UserDatabase database = Room.databaseBuilder(getApplicationContext(),
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        UserAccount currentUser = database.userDao().searchByUsername(username);

        if (currentUser != null) {
            // Set user values
            ImageView profileImg = findViewById(R.id.info_profile_img);
            profileImg.setImageBitmap(GeneralFunctions.getBitmap(currentUser.photo, this));
            TextView username = findViewById(R.id.info_username);
            username.setText(currentUser.username);
            TextView userId = findViewById(R.id.info_userid);
            userId.setText(String.format(Locale.ENGLISH, "#%d", currentUser.userId));
            TextView fullName = findViewById(R.id.info_full_name);
            fullName.setText(String.format(Locale.ENGLISH, "%s %s", currentUser.firstName,
                    currentUser.lastName));
            TextView creationDate = findViewById(R.id.info_creation_date);
            creationDate.setText(String.format(Locale.ENGLISH, "Date created: %s",
                    currentUser.creationDate));
            TextView birthday = findViewById(R.id.info_birthday);
            birthday.setText(String.format(Locale.ENGLISH, "Birthday: %s",
                    currentUser.birthday));
            Button adminPanel = findViewById(R.id.info_access_admin);
            if (! currentUser.isAdmin) {
                adminPanel.setVisibility(INVISIBLE);
            }
        }
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
            case R.id.info_edit:
                intent = new Intent(this, UserEditActivity.class);
                break;
            case R.id.info_access_admin:
                intent = new Intent(this, AdminActivity.class);
                break;
            default:
                intent = new Intent();
        }
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void onLogoutPressed(View view) {
        // Logs user out
        SharedPreferences.Editor editor = getSharedPreferences("com.pcchin.loginsys",
                MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Back to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
