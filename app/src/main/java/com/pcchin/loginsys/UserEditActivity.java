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

public class UserEditActivity extends AppCompatActivity {
    // TODO: Complete
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = savedInstanceState.getString("username", "");
        setContentView(R.layout.activity_user_edit);

        // Take values from user
        UserDatabase database = Room.databaseBuilder(getApplicationContext(),
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        UserAccount currentUser = database.userDao().searchByUsername(username);
        ((EditText) findViewById(R.id.edit_username_input)).setText(currentUser.username);
        ((EditText) findViewById(R.id.edit_firstname_input)).setText(currentUser.firstName);
        ((EditText) findViewById(R.id.edit_lastname_input)).setText(currentUser.lastName);

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

    public void onEditCancelPressed(View view) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
