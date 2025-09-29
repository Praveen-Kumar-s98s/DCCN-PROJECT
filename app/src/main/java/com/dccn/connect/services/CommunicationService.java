package com.dccn.connect.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.os.Build;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

import androidx.annotation.Nullable;

import com.dccn.connect.models.User;
import com.dccn.connect.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * CommunicationService - Handles mesh networking using Wi-Fi Direct and Bluetooth
 */
public class CommunicationService extends Service {
    
    private static final String TAG = "CommunicationService";
    
    // Wi-Fi P2P components
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;
    
    // Bluetooth components
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    
    // Service state
    private boolean isDiscoveryActive = false;
    private List<User> discoveredPeers = new ArrayList<>();
    private List<User> connectedPeers = new ArrayList<>();
    
    // Binder for activity binding
    private final IBinder binder = new LocalBinder();
    
    // Callbacks
    private OnPeerDiscoveryListener discoveryListener;
    private OnConnectionStatusListener connectionListener;
    
    public interface OnPeerDiscoveryListener {
        void onPeerDiscovered(User peer);
        void onPeerLost(User peer);
    }
    
    public interface OnConnectionStatusListener {
        void onPeerConnected(User peer);
        void onPeerDisconnected(User peer);
    }
    
    public class LocalBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CommunicationService created");
        
        // Initialize Wi-Fi P2P
        initWifiP2P();
        
        // Initialize Bluetooth
        initBluetooth();
        
        // Register broadcast receivers
        registerReceivers();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CommunicationService started");
        
        // Start discovery
        startDiscovery();
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CommunicationService destroyed");
        
        // Stop discovery
        stopDiscovery();
        
        // Unregister receivers
        unregisterReceivers();
    }
    
    /**
     * Initialize Wi-Fi P2P components
     */
    private void initWifiP2P() {
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager != null) {
            channel = wifiP2pManager.initialize(this, getMainLooper(), null);
            
            // Set up peer list listener
            peerListListener = new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    handleWifiPeersDiscovered(peers);
                }
            };
        }
    }
    
    /**
     * Initialize Bluetooth components
     */
    private void initBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }
    
    /**
     * Register broadcast receivers
     */
    private void registerReceivers() {
        // Wi-Fi P2P receivers
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, wifiFilter);
        
        // Bluetooth receivers
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, bluetoothFilter);
    }
    
    /**
     * Unregister broadcast receivers
     */
    private void unregisterReceivers() {
        try {
            unregisterReceiver(wifiReceiver);
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver not registered");
        }
    }
    
    /**
     * Start peer discovery
     */
    public void startDiscovery() {
        if (isDiscoveryActive) {
            return;
        }
        
        if (!hasServicePermissions()) {
            Log.w(TAG, "Missing runtime permissions; aborting discovery");
            return;
        }

        isDiscoveryActive = true;
        Log.d(TAG, "Starting peer discovery");
        
        // Start Wi-Fi P2P discovery
        if (wifiP2pManager != null && channel != null) {
            try {
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Wi-Fi P2P discovery started");
                }
                
                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Wi-Fi P2P discovery failed: " + reason);
                }
                });
            } catch (Exception e) {
                Log.e(TAG, "discoverPeers threw", e);
            }
        }
        
        // Start Bluetooth discovery
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                bluetoothAdapter.startDiscovery();
                Log.d(TAG, "Bluetooth discovery started");
            } catch (SecurityException se) {
                Log.e(TAG, "Bluetooth discovery security exception", se);
            }
        }
    }
    
    /**
     * Stop peer discovery
     */
    public void stopDiscovery() {
        if (!isDiscoveryActive) {
            return;
        }
        
        isDiscoveryActive = false;
        Log.d(TAG, "Stopping peer discovery");
        
        // Stop Wi-Fi P2P discovery
        if (wifiP2pManager != null && channel != null) {
            try {
                wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Wi-Fi P2P discovery stopped");
                }
                
                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Wi-Fi P2P discovery stop failed: " + reason);
                }
                });
            } catch (Exception e) {
                Log.e(TAG, "stopPeerDiscovery threw", e);
            }
        }
        
        // Stop Bluetooth discovery
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            try {
                bluetoothAdapter.cancelDiscovery();
            } catch (SecurityException se) {
                Log.e(TAG, "Bluetooth cancelDiscovery security exception", se);
            }
            Log.d(TAG, "Bluetooth discovery stopped");
        }
    }

    private boolean hasServicePermissions() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return fine;
        boolean scan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED;
        boolean connect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
        return fine && scan && connect;
    }
    
    /**
     * Handle Wi-Fi P2P peers discovered
     */
    private void handleWifiPeersDiscovered(WifiP2pDeviceList peers) {
        Log.d(TAG, "Wi-Fi P2P peers discovered: " + peers.getDeviceList().size());
        
        for (WifiP2pDevice device : peers.getDeviceList()) {
            User peer = createUserFromWifiDevice(device);
            if (peer != null) {
                addDiscoveredPeer(peer);
            }
        }
    }
    
    /**
     * Create User object from Wi-Fi P2P device
     */
    private User createUserFromWifiDevice(WifiP2pDevice device) {
        User user = new User();
        user.setUsername("WiFi_" + device.deviceName);
        user.setDeviceId(device.deviceAddress);
        user.setDeviceName(device.deviceName);
        user.setDeviceAddress(device.deviceAddress);
        user.setUserType(User.USER_TYPE_STUDENT); // Default to student
        user.setOnline(true);
        user.updateLastSeen();
        
        return user;
    }
    
    /**
     * Add discovered peer to list
     */
    private void addDiscoveredPeer(User peer) {
        if (!discoveredPeers.contains(peer)) {
            discoveredPeers.add(peer);
            Log.d(TAG, "New peer discovered: " + peer.getUsername());
            
            if (discoveryListener != null) {
                discoveryListener.onPeerDiscovered(peer);
            }
        }
    }
    
    /**
     * Set discovery listener
     */
    public void setOnPeerDiscoveryListener(OnPeerDiscoveryListener listener) {
        this.discoveryListener = listener;
    }
    
    /**
     * Set connection status listener
     */
    public void setOnConnectionStatusListener(OnConnectionStatusListener listener) {
        this.connectionListener = listener;
    }
    
    /**
     * Get discovered peers
     */
    public List<User> getDiscoveredPeers() {
        return new ArrayList<>(discoveredPeers);
    }
    
    /**
     * Get connected peers
     */
    public List<User> getConnectedPeers() {
        return new ArrayList<>(connectedPeers);
    }
    
    /**
     * Toggle Bluetooth and Wi-Fi
     */
    public void toggleBluetoothWifi() {
        // Toggle Bluetooth
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                Log.d(TAG, "Bluetooth disabled");
            } else {
                bluetoothAdapter.enable();
                Log.d(TAG, "Bluetooth enabled");
            }
        }
        
        // Note: Wi-Fi Direct doesn't have a simple enable/disable toggle
        // It's managed by the system based on discovery and connection state
        Log.d(TAG, "Wi-Fi Direct state managed by system");
    }
    
    /**
     * Send message to connected peers
     */
    public void sendMessage(com.dccn.connect.models.Message message) {
        Log.d(TAG, "Sending message: " + message.getText());
        
        // TODO: Implement actual message sending via Bluetooth/Wi-Fi Direct
        // For now, just log the message
        for (User peer : connectedPeers) {
            Log.d(TAG, "Message sent to peer: " + peer.getUsername());
        }
        
        // In a real implementation, this would:
        // 1. Serialize the message
        // 2. Send via Bluetooth or Wi-Fi Direct to each connected peer
        // 3. Handle delivery confirmation
    }
    
    /**
     * Send emergency alert to all connected peers
     */
    public void sendEmergencyAlert(com.dccn.connect.models.Message emergencyMessage) {
        Log.d(TAG, "Sending EMERGENCY ALERT: " + emergencyMessage.getText());
        
        // Emergency alerts have higher priority
        for (User peer : connectedPeers) {
            Log.d(TAG, "EMERGENCY ALERT sent to peer: " + peer.getUsername());
        }
        
        // Also send to discovered peers if they're not connected yet
        for (User peer : discoveredPeers) {
            if (!connectedPeers.contains(peer)) {
                Log.d(TAG, "EMERGENCY ALERT sent to discovered peer: " + peer.getUsername());
            }
        }
        
        // In a real implementation, this would:
        // 1. Use higher priority transmission
        // 2. Retry multiple times for reliability
        // 3. Use both Bluetooth and Wi-Fi Direct for maximum reach
        // 4. Trigger device vibrations/notifications
    }
    
    /**
     * Wi-Fi P2P broadcast receiver
     */
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "Wi-Fi P2P enabled");
                } else {
                    Log.d(TAG, "Wi-Fi P2P disabled");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (wifiP2pManager != null) {
                    wifiP2pManager.requestPeers(channel, peerListListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Handle device changes
            }
        }
    };
    
    /**
     * Bluetooth broadcast receiver
     */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    User peer = createUserFromBluetoothDevice(device);
                    if (peer != null) {
                        addDiscoveredPeer(peer);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery finished");
            }
        }
    };
    
    /**
     * Create User object from Bluetooth device
     */
    private User createUserFromBluetoothDevice(BluetoothDevice device) {
        User user = new User();
        user.setUsername("BT_" + device.getName());
        user.setDeviceId(device.getAddress());
        user.setDeviceName(device.getName());
        user.setDeviceAddress(device.getAddress());
        user.setUserType(User.USER_TYPE_STUDENT); // Default to student
        user.setOnline(true);
        user.updateLastSeen();
        
        return user;
    }
}

