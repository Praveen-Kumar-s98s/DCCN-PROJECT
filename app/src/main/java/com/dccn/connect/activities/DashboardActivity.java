package com.dccn.connect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;
import com.dccn.connect.adapters.PeerAdapter;
import com.dccn.connect.models.User;
import com.dccn.connect.services.CommunicationService;
import com.dccn.connect.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardActivity - Main dashboard showing network status and connected peers
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvNetworkStatus;
    private TextView tvPeerCount;
    private Button btnStartDiscovery;
    private Button btnSendMessage;
    private Button btnEmergencyAlert;
    private Button btnEnableBluetoothWifi;
    private Button btnLogout;
    private RecyclerView rvConnectedPeers;
    
    private PreferenceManager preferenceManager;
    private User currentUser;
    private PeerAdapter peerAdapter;
    private List<User> connectedPeers;
    private boolean isDiscoveryActive = false;

    // Permission request codes
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);
        
        // Get current user
        currentUser = preferenceManager.getUser();
        if (currentUser == null) {
            // No user logged in, return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initViews();
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Set up click listeners
        setupClickListeners();
        
        // Update UI
        updateUI();
        
        // Check and request permissions
        checkPermissions();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvPeerCount = findViewById(R.id.tv_peer_count);
        btnStartDiscovery = findViewById(R.id.btn_start_discovery);
        btnSendMessage = findViewById(R.id.btn_send_message);
        btnEmergencyAlert = findViewById(R.id.btn_emergency_alert);
        btnEnableBluetoothWifi = findViewById(R.id.btn_enable_bluetooth_wifi);
        btnLogout = findViewById(R.id.btn_logout);
        rvConnectedPeers = findViewById(R.id.rv_connected_peers);
    }

    /**
     * Set up RecyclerView for displaying connected peers
     */
    private void setupRecyclerView() {
        connectedPeers = new ArrayList<>();
        peerAdapter = new PeerAdapter(connectedPeers);
        
        rvConnectedPeers.setLayoutManager(new LinearLayoutManager(this));
        rvConnectedPeers.setAdapter(peerAdapter);
    }

    /**
     * Set up click listeners for all buttons
     */
    private void setupClickListeners() {
        btnStartDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDiscovery();
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat();
            }
        });

        btnEmergencyAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmergencyAlert();
            }
        });

        btnEnableBluetoothWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBluetoothWifi();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     * Update UI with current user and network information
     */
    private void updateUI() {
        if (currentUser != null) {
            tvUsername.setText("Welcome, " + currentUser.getUsername());
        }
        
        // Update network status (simulated for now)
        updateNetworkStatus();
        
        // Update peer count
        updatePeerCount();
    }

    /**
     * Update network status display
     */
    private void updateNetworkStatus() {
        // Simulated network status
        tvNetworkStatus.setText("Connected");
        tvNetworkStatus.setTextColor(getResources().getColor(R.color.network_connected));
    }

    /**
     * Update peer count display
     */
    private void updatePeerCount() {
        int peerCount = connectedPeers.size();
        tvPeerCount.setText(peerCount + " peers connected");
    }

    /**
     * Toggle network discovery on/off
     */
    private void toggleDiscovery() {
        if (!isDiscoveryActive) {
            startDiscovery();
        } else {
            stopDiscovery();
        }
    }

    /**
     * Start network discovery
     */
    private void startDiscovery() {
        isDiscoveryActive = true;
        btnStartDiscovery.setText("Stop Discovery");
        btnStartDiscovery.setBackgroundColor(getResources().getColor(R.color.error_500));
        
        // Start communication service
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        startService(serviceIntent);
        
        Toast.makeText(this, "Network discovery started", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop network discovery
     */
    private void stopDiscovery() {
        isDiscoveryActive = false;
        btnStartDiscovery.setText("Start Discovery");
        btnStartDiscovery.setBackgroundColor(getResources().getColor(R.color.primary_500));
        
        // Stop communication service
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        stopService(serviceIntent);
        
        Toast.makeText(this, "Network discovery stopped", Toast.LENGTH_SHORT).show();
    }

    /**
     * Open chat activity
     */
    private void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    /**
     * Open emergency alert activity
     */
    private void openEmergencyAlert() {
        Intent intent = new Intent(this, EmergencyAlertActivity.class);
        startActivity(intent);
    }

    /**
     * Toggle Bluetooth and Wi-Fi
     */
    private void toggleBluetoothWifi() {
        // This would implement actual Bluetooth and Wi-Fi toggling
        Toast.makeText(this, "Bluetooth & Wi-Fi toggled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Logout user and return to main activity
     */
    private void logout() {
        // Clear user data
        preferenceManager.clearUser();
        
        // Return to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Check and request necessary permissions
     */
    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

