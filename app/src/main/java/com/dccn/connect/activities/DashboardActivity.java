package com.dccn.connect.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;
import com.dccn.connect.adapters.FoundDeviceAdapter;
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

    private TextView tvNetworkStatus;
    private TextView tvPeerCount;
    private Button btnStartDiscovery;
    private Button btnSendMessage;
    private Button btnEmergencyAlert;
    private Button btnEnableBluetoothWifi;
    private Button btnLogout;
    private RecyclerView rvConnectedPeers;
    
    // Scanner overlay components
    private View scannerOverlay;
    private ImageView scannerWaves;
    private ImageView scannerCircleInner;
    private TextView tvScanningStatus;
    private TextView tvScanningProgress;
    private RecyclerView rvFoundDevices;
    private Button btnStopScan;
    
    private PreferenceManager preferenceManager;
    private User currentUser;
    private PeerAdapter peerAdapter;
    private FoundDeviceAdapter foundDeviceAdapter;
    private List<User> connectedPeers;
    private List<User> discoveredPeers;
    private boolean isDiscoveryActive = false;
    private CommunicationService communicationService;
    private boolean isServiceBound = false;

    // Permission request codes
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQ_PERMS = 1001;
    
    // Service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) service;
            communicationService = binder.getService();
            isServiceBound = true;
            
            // Set up listeners
            communicationService.setOnConnectionStatusListener(new CommunicationService.OnConnectionStatusListener() {
                @Override
                public void onConnectionStatusChanged(boolean isConnected) {
                    runOnUiThread(() -> {
                        updateNetworkStatus();
                    });
                }
                
                @Override
                public void onPeerCountChanged(int peerCount) {
                    runOnUiThread(() -> {
                        updatePeerCount();
                    });
                }
            });
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
        
        // Check and request permissions (Android 12+ requires BT runtime permissions)
        ensureRuntimePermissions();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        TextView tvUsername = findViewById(R.id.tv_username);
        tvUsername.setText("Welcome, " + currentUser.getUsername());
        
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvPeerCount = findViewById(R.id.tv_peer_count);
        btnStartDiscovery = findViewById(R.id.btn_start_discovery);
        btnSendMessage = findViewById(R.id.btn_send_message);
        btnEmergencyAlert = findViewById(R.id.btn_emergency_alert);
        btnEnableBluetoothWifi = findViewById(R.id.btn_enable_bluetooth_wifi);
        btnLogout = findViewById(R.id.btn_logout);
        rvConnectedPeers = findViewById(R.id.rv_connected_peers);
        
        // Initialize scanner overlay components
        scannerOverlay = findViewById(R.id.scanner_overlay);
        scannerWaves = scannerOverlay.findViewById(R.id.scanner_waves);
        scannerCircleInner = scannerOverlay.findViewById(R.id.scanner_circle);
        tvScanningStatus = scannerOverlay.findViewById(R.id.tv_scanning_status);
        tvScanningProgress = scannerOverlay.findViewById(R.id.tv_scanning_progress);
        rvFoundDevices = scannerOverlay.findViewById(R.id.rv_found_devices);
        btnStopScan = scannerOverlay.findViewById(R.id.btn_stop_scan);
    }

    /**
     * Set up RecyclerView for displaying connected peers
     */
    private void setupRecyclerView() {
        connectedPeers = new ArrayList<>();
        discoveredPeers = new ArrayList<>();
        peerAdapter = new PeerAdapter(connectedPeers);
        
        rvConnectedPeers.setLayoutManager(new LinearLayoutManager(this));
        rvConnectedPeers.setAdapter(peerAdapter);
        
        // Set up found devices RecyclerView
        foundDeviceAdapter = new FoundDeviceAdapter();
        rvFoundDevices.setLayoutManager(new LinearLayoutManager(this));
        rvFoundDevices.setAdapter(foundDeviceAdapter);
        
        // Set up peer click listener
        peerAdapter.setOnPeerClickListener(new PeerAdapter.OnPeerClickListener() {
            @Override
            public void onPeerClick(User peer) {
                // Connect to the selected peer
                if (communicationService != null) {
                    // TODO: Implement peer connection
                    Toast.makeText(DashboardActivity.this, "Connecting to " + peer.getUsername(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Set up found device click listener
        foundDeviceAdapter.setOnDeviceClickListener(new FoundDeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(FoundDeviceAdapter.DiscoveredDevice device) {
                Toast.makeText(DashboardActivity.this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Implement device connection
            }
        });
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
        
        // Scanner overlay click listener
        btnStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDiscovery();
            }
        });
    }

    /**
     * Update UI with current user and network information
     */
    private void updateUI() {
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
        int connectedCount = connectedPeers.size();
        int discoveredCount = discoveredPeers.size();
        tvPeerCount.setText(connectedCount + " connected, " + discoveredCount + " discovered");
    }
    
    /**
     * Add discovered peer
     */
    private void addDiscoveredPeer(User peer) {
        if (!discoveredPeers.contains(peer)) {
            discoveredPeers.add(peer);
            updatePeerCount();
            Toast.makeText(this, "Discovered: " + peer.getUsername(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Remove discovered peer
     */
    private void removeDiscoveredPeer(User peer) {
        if (discoveredPeers.remove(peer)) {
            updatePeerCount();
        }
    }
    
    /**
     * Add connected peer
     */
    private void addConnectedPeer(User peer) {
        if (!connectedPeers.contains(peer)) {
            connectedPeers.add(peer);
            peerAdapter.notifyItemInserted(connectedPeers.size() - 1);
            updatePeerCount();
            Toast.makeText(this, "Connected to: " + peer.getUsername(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Remove connected peer
     */
    private void removeConnectedPeer(User peer) {
        int index = connectedPeers.indexOf(peer);
        if (index != -1) {
            connectedPeers.remove(index);
            peerAdapter.notifyItemRemoved(index);
            updatePeerCount();
        }
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
        // ensure permissions first
        if (!hasDiscoveryPermissions()) {
            ensureRuntimePermissions();
            return;
        }
        isDiscoveryActive = true;
        
        // Show scanner overlay and start animations
        showScannerOverlay();
        startScannerAnimations();
        
        // Start and bind to communication service
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // Simulate device discovery after some delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                simulateDeviceDiscovery();
            }
        }, 3000);
        
        Toast.makeText(this, "Network discovery started", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop network discovery
     */
    private void stopDiscovery() {
        isDiscoveryActive = false;
        
        // Hide scanner overlay and stop animations
        hideScannerOverlay();
        stopScannerAnimations();
        
        // Stop discovery in service
        if (communicationService != null) {
            communicationService.stopDiscovery();
        }
        
        // Unbind and stop communication service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        stopService(serviceIntent);
        
        // Clear discovered peers
        discoveredPeers.clear();
        foundDeviceAdapter.clearDevices();
        updatePeerCount();
        
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
        if (communicationService != null) {
            // Toggle Bluetooth and Wi-Fi through the service
            communicationService.toggleBluetoothWifi();
            Toast.makeText(this, "Bluetooth & Wi-Fi toggled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service not available", Toast.LENGTH_SHORT).show();
        }
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
    private void ensureRuntimePermissions() {
        List<String> missing = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), REQ_PERMS);
        }
    }

    private boolean hasDiscoveryPermissions() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return fine;
        }
        boolean scan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED;
        boolean connect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
        return fine && scan && connect;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMS || requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions required for discovery", Toast.LENGTH_LONG).show();
            }
        }
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
    
    /**
     * Show scanner overlay with animations
     */
    private void showScannerOverlay() {
        scannerOverlay.setVisibility(View.VISIBLE);
        tvScanningStatus.setText("Scanning for devices...");
        tvScanningProgress.setText("Searching...");
        foundDeviceAdapter.clearDevices();
    }
    
    /**
     * Hide scanner overlay
     */
    private void hideScannerOverlay() {
        scannerOverlay.setVisibility(View.GONE);
    }
    
    /**
     * Start scanner wave animations
     */
    private void startScannerAnimations() {
        if (scannerWaves != null) {
            ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(scannerWaves, "scaleX", 0.5f, 1.8f);
            ObjectAnimator scaleAnimY = ObjectAnimator.ofFloat(scannerWaves, "scaleY", 0.5f, 1.8f);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(scannerWaves, "alpha", 1.0f, 0.0f);
            
            scaleAnim.setDuration(2000);
            scaleAnimY.setDuration(2000);
            alphaAnim.setDuration(2000);
            
            scaleAnim.setRepeatCount(ObjectAnimator.INFINITE);
            scaleAnimY.setRepeatCount(ObjectAnimator.INFINITE);
            alphaAnim.setRepeatCount(ObjectAnimator.INFINITE);
            
            scaleAnim.start();
            scaleAnimY.start();
            alphaAnim.start();
        }
    }
    
    /**
     * Stop scanner wave animations
     */
    private void stopScannerAnimations() {
        if (scannerWaves != null) {
            scannerWaves.clearAnimation();
            scannerWaves.setScaleX(1.0f);
            scannerWaves.setScaleY(1.0f);
            scannerWaves.setAlpha(1.0f);
        }
    }
    
    /**
     * Simulate device discovery for demo purposes
     */
    private void simulateDeviceDiscovery() {
        // Add some mock devices for demonstration
        FoundDeviceAdapter.DiscoveredDevice device1 = 
            new FoundDeviceAdapter.DiscoveredDevice("Samsung Galaxy A12", "AA:BB:CC:DD:EE:FF", "Bluetooth");
        FoundDeviceAdapter.DiscoveredDevice device2 = 
            new FoundDeviceAdapter.DiscoveredDevice("iPhone 14", "11:22:33:44:55:66", "WiFi Direct");
        
        foundDeviceAdapter.addDevice(device1);
        foundDeviceAdapter.addDevice(device2);
        
        tvScanningProgress.setText("Found " + foundDeviceAdapter.getItemCount() + " devices");
    }
}

