package com.dccn.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dccn.connect.R;
import com.dccn.connect.models.User;
import com.dccn.connect.utils.PreferenceManager;

/**
 * RescueTeamLoginActivity - Handles rescue team authentication using username and password
 */
public class RescueTeamLoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private PreferenceManager preferenceManager;

    // Default rescue team credentials (in real app, this would be stored securely)
    private static final String DEFAULT_USERNAME = "rescue";
    private static final String DEFAULT_PASSWORD = "team123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_team_login);

        // Initialize PreferenceManager for storing user data
        preferenceManager = new PreferenceManager(this);

        // Initialize views
        initViews();
        
        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
    }

    /**
     * Set up click listeners for the buttons
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRescueTeamLogin();
            }
        });
    }

    /**
     * Handle rescue team login process
     */
    private void handleRescueTeamLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Please enter username");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter password");
            return;
        }

        // Check credentials (in real app, this would be against a secure database)
        if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
            // Create user object
            User user = new User();
            user.setUsername(username);
            user.setUserType(User.USER_TYPE_RESCUE_TEAM);
            user.setDeviceId(android.provider.Settings.Secure.getString(
                getContentResolver(), 
                android.provider.Settings.Secure.ANDROID_ID
            ));

            // Save user data
            preferenceManager.saveUser(user);

            // Show success message
            Toast.makeText(this, "Welcome, " + user.getUsername(), Toast.LENGTH_SHORT).show();

            // Navigate to Dashboard (keep back stack so user can return if something fails)
            Intent intent = new Intent(RescueTeamLoginActivity.this, DashboardActivity.class);
            startActivity(intent);
        } else {
            // Show error message
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            etPassword.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Return to main activity
    }
}

