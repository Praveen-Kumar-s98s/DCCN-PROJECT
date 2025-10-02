package com.dccn.connect.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dccn.connect.models.Message;
import com.dccn.connect.models.User;

import java.util.ArrayList;
import java.util.List;

public class CommunicationService extends Service {

    private static final String TAG = "CommunicationService";

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver wifiReceiver;
    private BroadcastReceiver bluetoothReceiver;
    private IntentFilter intentFilter;

    private final IBinder binder = new LocalBinder();
    private List<WifiP2pDevice> discoveredPeers = new ArrayList<>();
    private boolean isConnecting = false; // prevent multiple simultaneous connects

    public interface OnPeerDiscoveryListener {
        void onPeerDiscovered(User user);
    }

    private OnPeerDiscoveryListener onPeerDiscoveryListener;

    public void setOnPeerDiscoveryListener(OnPeerDiscoveryListener listener) {
        this.onPeerDiscoveryListener = listener;
    }

    // Binder class
    public class LocalBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Wi-Fi P2P manager
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager != null) {
            channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        }
        
        // Initialize broadcast receiver for Wi-Fi P2P events
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public void onDestroy() {
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
        }
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
        super.onDestroy();
    }

    // Start peer discovery
    public void startPeerDiscovery() {
        // Start WiFi P2P discovery
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "WiFi P2P peer discovery started successfully");
                    startBluetoothDiscovery();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e(TAG, "Failed to start WiFi P2P peer discovery: " + reasonCode);
                    startBluetoothDiscovery(); // Try Bluetooth anyway
                }
            });
        } else {
            startBluetoothDiscovery();
        }
    }

    // Start Bluetooth device discovery
    private void startBluetoothDiscovery() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isDiscovering()) {
                Log.d(TAG, "Starting Bluetooth discovery...");
                bluetoothAdapter.startDiscovery();
                
                // Register receiver for discovered devices
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(bluetoothReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (device != null && device.getName() != null) {
                                Log.d(TAG, "Found Bluetooth device: " + device.getName() + " (" + device.getAddress() + ")");
                                
                                // Notify discovered device
                                if (onConnectionStatusListener != null) {
                                    DiscoveredPeer peer = new DiscoveredPeer(device.getName(), device.getAddress());
                                    onConnectionStatusListener.onPeerDiscovered(peer);
                                }
                            }
                        }
                    }
                }, filter);
            }
        } else {
            Log.w(TAG, "Bluetooth adapter not available or disabled");
        }
    }

    // Stop peer discovery
    public void stopPeerDiscovery() {
        // Stop WiFi P2P discovery
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "WiFi P2P peer discovery stopped successfully");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e(TAG, "Failed to stop WiFi P2P peer discovery: " + reasonCode);
                }
            });
        }
        
        // Stop Bluetooth discovery
        stopBluetoothDiscovery();
    }

    // Stop Bluetooth device discovery
    private void stopBluetoothDiscovery() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Bluetooth discovery stopped");
        }
        
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
            bluetoothReceiver = null;
        }
    }

    // Additional methods for service functionality
    public void toggleBluetoothWifi() {
        // TODO: Implement Bluetooth/WiFi toggle functionality
    }

    public void stopDiscovery() {
        stopPeerDiscovery();
    }

    public List<DiscoveredPeer> getDiscoveredPeers() {
        // TODO: Implement peer discovery result handling
        return new ArrayList<>();
    }

    // Message sending methods
    public void sendMessage(Message message) {
        // TODO: Implement message sending functionality
        Log.d(TAG, "Sending message: " + message.getText());
    }

    public void sendEmergencyAlert(Message message) {
        // TODO: Implement emergency alert functionality
        Log.d(TAG, "Sending emergency alert: " + message.getText());
    }

    // Status listener interface
    public interface OnConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
        void onPeerCountChanged(int peerCount);
        void onPeerDiscovered(DiscoveredPeer peer);
    }

    private OnConnectionStatusListener onConnectionStatusListener;

    public void setOnConnectionStatusListener(OnConnectionStatusListener listener) {
        this.onConnectionStatusListener = listener;
    }

    public static class DiscoveredPeer {
        private String name;
        private String address;
        
        public DiscoveredPeer(String name, String address) {
            this.name = name;
            this.address = address;
        }
        
        public String getName() { return name; }
        public String getAddress() { return address; }
    }

}
