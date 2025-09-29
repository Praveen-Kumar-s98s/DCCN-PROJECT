package com.dccn.connect.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dccn.connect.R;
import com.dccn.connect.models.Message;
import com.dccn.connect.models.User;
import com.dccn.connect.services.CommunicationService;
import com.dccn.connect.utils.PreferenceManager;

public class EmergencyAlertActivity extends AppCompatActivity {
    
    private EditText emergencyMessageInput;
    private CommunicationService communicationService;
    private boolean isServiceBound = false;
    private User currentUser;
    private PreferenceManager preferenceManager;
    
    // Service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) service;
            communicationService = binder.getService();
            isServiceBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            communicationService = null;
            isServiceBound = false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);
        
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);
        currentUser = preferenceManager.getUser();
        
        initViews();
        
        // Bind to communication service
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
                    sendEmergencyAlert(message);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void sendEmergencyAlert(String message) {
        if (communicationService != null) {
            String senderName = currentUser != null ? currentUser.getUsername() : "Unknown";
            String emergencyText = "🚨 EMERGENCY ALERT 🚨\n" + message + "\n\nFrom: " + senderName;
            
            Message emergencyMessage = new Message(emergencyText, senderName, System.currentTimeMillis());
            communicationService.sendEmergencyAlert(emergencyMessage);
            
            Toast.makeText(this, "Emergency alert sent to all connected peers!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Service not available", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind service if bound
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}
