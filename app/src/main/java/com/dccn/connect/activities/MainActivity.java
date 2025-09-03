package com.dccn.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dccn.connect.R;

/**
 * MainActivity - Entry point for DCCN Connect app
 * Provides options for Student Login and Rescue Team Login
 */
public class MainActivity extends AppCompatActivity {

    private CardView cardStudentLogin;
    private CardView cardRescueLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();
        
        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        cardStudentLogin = findViewById(R.id.card_student_login);
        cardRescueLogin = findViewById(R.id.card_rescue_login);
    }

    /**
     * Set up click listeners for the login cards
     */
    private void setupClickListeners() {
        // Student Login Card Click Listener
        cardStudentLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Student Login Activity
                Intent intent = new Intent(MainActivity.this, StudentLoginActivity.class);
                startActivity(intent);
            }
        });

        // Rescue Team Login Card Click Listener
        cardRescueLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Rescue Team Login Activity
                Intent intent = new Intent(MainActivity.this, RescueTeamLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset any animations or states if needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clean up resources if needed
    }
}

