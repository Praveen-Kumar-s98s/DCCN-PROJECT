package com.dccn.connect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dccn.connect.R;

public class EmergencyAlertActivity extends AppCompatActivity {
    
    private EditText emergencyMessageInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);
        
        initViews();
    }
    
    private void initViews() {
        emergencyMessageInput = findViewById(R.id.emergency_message_input);
    }
    
    public void sendEmergencyAlert(View view) {
        String message = emergencyMessageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            showConfirmationDialog(message);
        } else {
            Toast.makeText(this, "Please enter an emergency message", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showConfirmationDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to send this emergency alert to all connected peers?")
                .setPositiveButton("Send", (dialog, which) -> {
                    // TODO: Implement emergency alert sending
                    Toast.makeText(this, "Emergency alert sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
