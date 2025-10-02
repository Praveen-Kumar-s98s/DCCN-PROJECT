package com.dccn.connect.adapters;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;

import java.util.ArrayList;
import java.util.List;

public class FoundDeviceAdapter extends RecyclerView.Adapter<FoundDeviceAdapter.DeviceViewHolder> {

    private List<DiscoveredDevice> devices = new ArrayList<>();
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(DiscoveredDevice device);
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public static class DiscoveredDevice {
        private String name;
        private String address;
        private String type; // "Bluetooth", "WiFi Direct"
        private int signalStrength;
        private boolean isConnected;

        public DiscoveredDevice(String name, String address, String type) {
            this.name = name;
            this.address = address;
            this.type = type;
            this.signalStrength = -50; // Default signal strength
            this.isConnected = false;
        }

        // Getters and setters
        public String getName() { return name == null || name.isEmpty() ? "Unknown Device" : name; }
        public String getAddress() { return address; }
        public String getType() { return type; }
        public int getSignalStrength() { return signalStrength; }
        public boolean isConnected() { return isConnected; }
        
        public void setName(String name) { this.name = name; }
        public void setSignalStrength(int signalStrength) { this.signalStrength = signalStrength; }
        public void setConnected(boolean connected) { this.isConnected = connected; }

        // Create from Bluetooth device
        public static DiscoveredDevice fromBluetoothDevice(BluetoothDevice device) {
            return new DiscoveredDevice(device.getName(), device.getAddress(), "Bluetooth");
        }

        // Create from WiFi Direct device
        public static DiscoveredDevice fromWifiP2pDevice(WifiP2pDevice device) {
            return new DiscoveredDevice(device.deviceName, device.deviceAddress, "WiFi Direct");
        }
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDeviceName;
        private TextView tvDeviceType;
        private TextView tvSignalStrength;
        private View connectionStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceType = itemView.findViewById(R.id.tv_device_type);
            tvSignalStrength = itemView.findViewById(R.id.tv_signal_strength);
            connectionStatus = itemView.findViewById(R.id.view_connection_status);
        }

        public void bind(DiscoveredDevice device) {
            tvDeviceName.setText(device.getName());
            tvDeviceType.setText(device.getType());
            tvSignalStrength.setText(getSignalText(device.getSignalStrength()));
            
            // Update connection status indicator
            if (device.isConnected()) {
                connectionStatus.setBackgroundResource(R.drawable.status_indicator_connected);
                tvDeviceName.setTextColor(itemView.getContext().getColor(R.color.success_500));
            } else {
                connectionStatus.setBackgroundResource(R.drawable.status_indicator_disconnected);
                tvDeviceName.setTextColor(itemView.getContext().getColor(R.color.white));
            }
        }

        private String getSignalText(int strength) {
            if (strength >= -30) return "Excellent";
            if (strength >= -50) return "Good";
            if (strength >= -70) return "Fair";
            return "Poor";
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_found_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DiscoveredDevice device = devices.get(position);
        holder.bind(device);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(DiscoveredDevice device) {
        // Check if device already exists
        for (DiscoveredDevice existing : devices) {
            if (existing.getAddress().equals(device.getAddress())) {
                return; // Device already exists
            }
        }
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public void removeDevice(String address) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getAddress().equals(address)) {
                devices.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateDevice(String address, int signalStrength, boolean isConnected) {
        for (int i = 0; i < devices.size(); i++) {
            DiscoveredDevice device = devices.get(i);
            if (device.getAddress().equals(address)) {
                device.setSignalStrength(signalStrength);
                device.setConnected(isConnected);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public List<DiscoveredDevice> getDevices() {
        return new ArrayList<>(devices);
    }
}
