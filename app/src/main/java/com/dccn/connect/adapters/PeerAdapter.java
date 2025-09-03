package com.dccn.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;
import com.dccn.connect.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * PeerAdapter - RecyclerView adapter for displaying connected peers
 */
public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.PeerViewHolder> {
    
    private List<User> peers;
    private OnPeerClickListener listener;
    private SimpleDateFormat dateFormat;
    
    public interface OnPeerClickListener {
        void onPeerClick(User peer);
    }
    
    public PeerAdapter(List<User> peers) {
        this.peers = peers;
        this.dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    public void setOnPeerClickListener(OnPeerClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_peer, parent, false);
        return new PeerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PeerViewHolder holder, int position) {
        User peer = peers.get(position);
        holder.bind(peer);
    }
    
    @Override
    public int getItemCount() {
        return peers.size();
    }
    
    /**
     * Update the peers list
     */
    public void updatePeers(List<User> newPeers) {
        this.peers.clear();
        if (newPeers != null) {
            this.peers.addAll(newPeers);
        }
        notifyDataSetChanged();
    }
    
    /**
     * Add a new peer
     */
    public void addPeer(User peer) {
        if (!peers.contains(peer)) {
            peers.add(peer);
            notifyItemInserted(peers.size() - 1);
        }
    }
    
    /**
     * Remove a peer
     */
    public void removePeer(User peer) {
        int index = peers.indexOf(peer);
        if (index != -1) {
            peers.remove(index);
            notifyItemRemoved(index);
        }
    }
    
    /**
     * Update a specific peer
     */
    public void updatePeer(User peer) {
        int index = peers.indexOf(peer);
        if (index != -1) {
            peers.set(index, peer);
            notifyItemChanged(index);
        }
    }
    
    /**
     * ViewHolder class for peer items
     */
    class PeerViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvPeerName;
        private TextView tvPeerType;
        private TextView tvDeviceInfo;
        private TextView tvLastSeen;
        private TextView tvSignalStrength;
        private View statusIndicator;
        
        public PeerViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvPeerName = itemView.findViewById(R.id.tv_peer_name);
            tvPeerType = itemView.findViewById(R.id.tv_peer_type);
            tvDeviceInfo = itemView.findViewById(R.id.tv_device_info);
            tvLastSeen = itemView.findViewById(R.id.tv_last_seen);
            tvSignalStrength = itemView.findViewById(R.id.tv_signal_strength);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            
            // Set click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onPeerClick(peers.get(position));
                    }
                }
            });
        }
        
        public void bind(User peer) {
            // Set peer name
            tvPeerName.setText(peer.getUsername());
            
            // Set peer type
            tvPeerType.setText(peer.getUserTypeString());
            
            // Set device info
            String deviceInfo = peer.getDeviceName();
            if (deviceInfo == null || deviceInfo.isEmpty()) {
                deviceInfo = peer.getDeviceId().substring(0, Math.min(8, peer.getDeviceId().length())) + "...";
            }
            tvDeviceInfo.setText(deviceInfo);
            
            // Set last seen
            String lastSeenText = "Last seen: " + dateFormat.format(new Date(peer.getLastSeen()));
            tvLastSeen.setText(lastSeenText);
            
            // Set signal strength
            tvSignalStrength.setText(peer.getSignalStrengthDescription());
            
            // Set online status indicator
            if (peer.isOnline()) {
                statusIndicator.setBackgroundResource(R.color.network_connected);
            } else {
                statusIndicator.setBackgroundResource(R.color.network_disconnected);
            }
            
            // Set text colors based on user type
            if (peer.isRescueTeam()) {
                tvPeerType.setTextColor(itemView.getContext().getResources().getColor(R.color.secondary_500));
            } else {
                tvPeerType.setTextColor(itemView.getContext().getResources().getColor(R.color.primary_500));
            }
        }
    }
}

