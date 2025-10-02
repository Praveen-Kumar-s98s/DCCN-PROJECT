package com.dccn.connect.services;

import android.app.Service;
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
        super.onDestroy();
    }

    // Start peer discovery
    public void startPeerDiscovery() {
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Peer discovery started successfully");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e(TAG, "Failed to start peer discovery: " + reasonCode);
                }
            });
        }
    }

    // Stop peer discovery
    public void stopPeerDiscovery() {
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Peer discovery stopped successfully");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e(TAG, "Failed to stop peer discovery: " + reasonCode);
                }
            });
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
