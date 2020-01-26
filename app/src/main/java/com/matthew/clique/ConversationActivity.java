package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.R;
import com.matthew.clique.adapters.MessagesRecyclerAdapter;
import com.matthew.clique.models.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private Toolkit tk;

    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String userId, conversationId, friendName;

    private ImageView sendMessage;
    private EditText messageField;

    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private List<Message> messageList;
    private List<Message> insertList;
    private RecyclerView messagesRecyclerView;

    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        tk = new Toolkit(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getUid();
        conversationId = getIntent().getStringExtra("conversation_id");

        //Toolbar setup
        friendName = getIntent().getStringExtra("friend_name");
        toolbar = findViewById(R.id.toolbarConversation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setTitle(friendName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //RecyclerView setup
        insertList = new ArrayList<>();
        messageList = new ArrayList<>();
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(messageList);
        messagesRecyclerView = findViewById(R.id.recyclerViewConversation);
        //todo change layout loading order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); //sets bottom as start
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesRecyclerView.setAdapter(messagesRecyclerAdapter);

        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Messages")
                .orderBy("time_sent")
                .addSnapshotListener(ConversationActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstLoad) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        messageList.add(0, message);
                                        messagesRecyclerAdapter.insertMessages(messageList);
                                    }
                                }
                            } else {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        messageList.add(0, message); //push to the start of the list
                                    }
                                }
                            }

                        }
                    }
                });

        //Listeners
        messageField = findViewById(R.id.editTextMessage);
        sendMessage = findViewById(R.id.imageViewSendButton);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageField.getText().toString().trim();
                sendMessage(text);
            }
        });

    }
    
    private void sendMessage(String text) {
        if (!text.isEmpty()) {
            messageField.getText().clear();

            String messageId = tk.generateUID(10);
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("message_id", messageId);
            messageMap.put("conversation_id", conversationId);
            messageMap.put("sender", userId);
            messageMap.put("message", text);
            messageMap.put("time_sent", FieldValue.serverTimestamp());

            firebaseFirestore
                    .collection("Conversations/" + conversationId + "/Messages")
                    .document(messageId)
                    .set(messageMap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
