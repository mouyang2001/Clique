package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.matthew.clique.fragments.MessageOptionsDialog;
import com.matthew.clique.models.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationActivity
        extends AppCompatActivity
        implements MessageOptionsDialog.MessageOptionsListener{

    private Toolkit tk;

    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String userId, conversationId, friendName;

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
        friendName = getIntent().getStringExtra("friend_name");
        toolbar = findViewById(R.id.toolbarConversation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setTitle(friendName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //RecyclerView setup
        messageList = new ArrayList<>();
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(messageList);
        messagesRecyclerView = findViewById(R.id.recyclerViewConversation);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); //solved everything
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesRecyclerView.setAdapter(messagesRecyclerAdapter);

        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Messages")
                .orderBy("time_sent")
                .addSnapshotListener(ConversationActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Message message = doc.getDocument().toObject(Message.class);
                                    messagesRecyclerAdapter.addMessage(message);
                                    messagesRecyclerView.smoothScrollToPosition(messagesRecyclerAdapter.getItemCount());
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
            messageMap.put("deleted", false);

            firebaseFirestore
                    .collection("Conversations/" + conversationId + "/Messages")
                    .document(messageId)
                    .set(messageMap);
        }
    }

    //Toolbar options control
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

    //Message options control
    @Override
    public void onMessageOptionClicked(String command, int messagePosition) {
        switch(command) {
            case "delete":
                deleteMessage(messagePosition);
                break;
            case "copy":
                copyMessage(messagePosition);
                break;
        }
    }

    private void deleteMessage(int messagePosition) {
        String messageId = messagesRecyclerAdapter.getMessages().get(messagePosition).getMessage_id();
        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Messages")
                .document(messageId)
                .update("deleted", true);
    }

    private void copyMessage(int messagePosition) {
        String text = messagesRecyclerAdapter.getMessages().get(messagePosition).getMessage();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text", text);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        clipboardManager.setPrimaryClip(clip);
    }
}
