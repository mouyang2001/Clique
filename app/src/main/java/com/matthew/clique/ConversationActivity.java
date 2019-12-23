package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private Toolkit tk;

    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String userId, conversationId;

    private ImageView sendMessage;
    private EditText messageField;

    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private List<Message> messageList;
    private RecyclerView messagesRecyclerView;

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
        toolbar = findViewById(R.id.toolbarConversation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("friend_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //RecyclerView setup
        messageList = new ArrayList<>();
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(messageList);
        messagesRecyclerView = findViewById(R.id.recyclerViewConversation);
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messagesRecyclerAdapter);

        //Adapter feed
        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Messages")
                .addSnapshotListener(ConversationActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String documentId = doc.getDocument().getId();

                                    if (!documentId.equals(userId)) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        messageList.add(message);
                                        messagesRecyclerAdapter.notifyDataSetChanged();
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
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("conversation_id", conversationId);
            messageMap.put("sender", userId);
            messageMap.put("message", text);
            messageMap.put("time_sent", FieldValue.serverTimestamp());

            firebaseFirestore
                    .collection("Conversations/" + conversationId + "/Messages")
                    .add(messageMap)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                messageField.getText().clear();
                            }
                        }
                    });
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
