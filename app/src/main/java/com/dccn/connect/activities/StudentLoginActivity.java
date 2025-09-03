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
 * StudentLoginActivity - Handles student authentication using register number
 */
public class StudentLoginActivity extends AppCompatActivity {

    private EditText etRegisterNumber;
    private Button btnEnter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

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
        etRegisterNumber = findViewById(R.id.et_register_number);
        btnEnter = findViewById(R.id.btn_enter);
    }

    /**
     * Set up click listeners for the buttons
     */
    private void setupClickListeners() {
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStudentLogin();
            }
        });
    }

    /**
     * Handle student login process
     */
    private void handleStudentLogin() {
        String registerNumber = etRegisterNumber.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(registerNumber)) {
            etRegisterNumber.setError("Please enter your register number");
            return;
        }

        if (registerNumber.length() < 3) {
            etRegisterNumber.setError("Register number must be at least 3 characters");
            return;
        }

        // Create user object
        User user = new User();
        user.setUsername("Student_" + registerNumber);
        user.setRegisterNumber(registerNumber);
        user.setUserType(User.USER_TYPE_STUDENT);
        user.setDeviceId(android.provider.Settings.Secure.getString(
            getContentResolver(), 
            android.provider.Settings.Secure.ANDROID_ID
        ));

        // Save user data
        preferenceManager.saveUser(user);

        // Show success message
        Toast.makeText(this, "Welcome, " + user.getUsername(), Toast.LENGTH_SHORT).show();

        // Navigate to Dashboard
        Intent intent = new Intent(StudentLoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Return to main activity
    }
}

