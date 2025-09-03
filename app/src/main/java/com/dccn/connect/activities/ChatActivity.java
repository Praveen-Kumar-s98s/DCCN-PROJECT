package com.dccn.connect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dccn.connect.R;
import com.dccn.connect.adapters.ChatAdapter;
import com.dccn.connect.models.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ChatAdapter adapter;
    private List<Message> messages;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        initViews();
        setupRecyclerView();
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
            Message message = new Message(text, "You", System.currentTimeMillis());
            messages.add(message);
            adapter.notifyItemInserted(messages.size() - 1);
            messageInput.setText("");
            recyclerView.scrollToPosition(messages.size() - 1);
        }
    }
}
