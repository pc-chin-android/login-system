package com.pcchin.loginsys;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.loginsys.database.UserAccount;
import com.pcchin.loginsys.database.UserDatabase;

import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        username = getIntent().getStringExtra("username");

        // List all users
        final UserDatabase database = Room.databaseBuilder(this,
                UserDatabase.class, "userAccount").allowMainThreadQueries().build();
        List<UserAccount> userList = database.userDao().getAllUser();
        LinearLayout mainLayout = findViewById(R.id.admin_layout);
        for (final UserAccount user: userList) {
            // Root is null as it will be attached later
            @SuppressLint("InflateParams") ConstraintLayout currentLayout =
                    (ConstraintLayout) getLayoutInflater()
                            .inflate(R.layout.layout_admin_user, null, false);

            // Set values
            ImageView profileImg = currentLayout.findViewById(R.id.admin_profileimg);
            profileImg.setImageBitmap(Bitmap.createScaledBitmap(GeneralFunctions.getBitmap(user.photo),
                    100, 100, false));

            ((TextView) currentLayout.findViewById(R.id.admin_fullname))
                    .setText(String.format("%s %s", user.firstName, user.lastName));
            ((TextView) currentLayout.findViewById(R.id.admin_username))
                    .setText(user.username);

            // Listener for warning dialog
            final DialogInterface.OnClickListener intListener = new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            database.userDao().delete(user);
                            Toast.makeText(AdminActivity.this,
                                    getString(R.string.user_deleted), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminActivity.this,
                                    UserInfoActivity.class);
                            startActivity(intent);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };

            // Listener for properties dialog
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(AdminActivity.this,
                                    UserInfoActivity.class);
                            intent.putExtra("username", user.username);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            if (username.equals(user.username)) {
                                // No deleting current user
                                Toast.makeText(AdminActivity.this,
                                        getString(R.string.error_acc_in_use), Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                // Display warning dialog for deleting user
                                new AlertDialog.Builder(AdminActivity.this)
                                        .setTitle(R.string.delete_user)
                                        .setPositiveButton(R.string.delete, intListener)
                                        .setNegativeButton(android.R.string.cancel, intListener)
                                        .create().show();
                            }
                            break;

                        case DialogInterface.BUTTON_NEUTRAL:
                            dialog.dismiss();
                            break;
                    }
                }
            };

            // Set up popup
            final AlertDialog currentDialog = new AlertDialog.Builder(this)
                    .setTitle(user.username)
                    .setPositiveButton(R.string.access, listener)
                    .setNeutralButton(android.R.string.cancel, listener)
                    .setNegativeButton(R.string.delete, listener)
                    .create();

            // Show popup
            currentLayout.findViewById(R.id.admin_user_properties)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentDialog.show();
                        }
                    });
            mainLayout.addView(currentLayout);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void onReturnPressed(View view) {
        this.onBackPressed();
    }
}
