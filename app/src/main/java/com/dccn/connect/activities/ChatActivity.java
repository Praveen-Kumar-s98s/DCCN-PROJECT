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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;
import com.dccn.connect.adapters.ChatAdapter;
import com.dccn.connect.models.Message;
import com.dccn.connect.models.User;
import com.dccn.connect.services.CommunicationService;
import com.dccn.connect.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ChatAdapter adapter;
    private List<Message> messages;
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
        setContentView(R.layout.activity_chat);
        
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);
        currentUser = preferenceManager.getUser();
        
        initViews();
        setupRecyclerView();
        
        // Bind to communication service
        Intent serviceIntent = new Intent(this, CommunicationService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.message_input);
    }
    
    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    public void sendMessage(View view) {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            String senderName = currentUser != null ? currentUser.getUsername() : "You";
            Message message = new Message(text, senderName, System.currentTimeMillis());
            messages.add(message);
            adapter.notifyItemInserted(messages.size() - 1);
            messageInput.setText("");
            recyclerView.scrollToPosition(messages.size() - 1);
            
            // Send message through communication service
            if (communicationService != null) {
                communicationService.sendMessage(message);
                Toast.makeText(this, "Message sent to connected peers", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Service not available", Toast.LENGTH_SHORT).show();
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
}
