package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matthew.clique.R;
import com.matthew.clique.adapters.MessagesRecyclerAdapter;
import com.matthew.clique.fragments.MessageOptionsDialog;
import com.matthew.clique.fragments.ProfileFragment;
import com.matthew.clique.models.Conversation;
import com.matthew.clique.models.Message;
import com.theartofdev.edmodo.cropper.CropImage;

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

    private ImageView sendButton, cameraButton;
    private EditText messageField;

    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private List<Message> messageList;
    private List<Message> deletedMessageList;
    private RecyclerView messagesRecyclerView;

    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        tk = new Toolkit(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        userId = firebaseAuth.getUid();
        conversationId = getIntent().getStringExtra("conversation_id");

        //Toolbar setup
        friendName = getIntent().getStringExtra("friend_name");
        toolbar = findViewById(R.id.toolbarConversation);
        setSupportActionBar(toolbar);
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

        //todo deleted message realtime

        //Listeners
        messageField = findViewById(R.id.editTextMessage);
        cameraButton = findViewById(R.id.imageViewCameraSend);
        sendButton = findViewById(R.id.imageViewSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageField.getText().toString().trim();
                sendMessage(text, false);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ConversationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }   else {
                        CropImage.activity()
                                .start(ConversationActivity.this);
                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String imageId = tk.generateUID(10);
                StorageReference imagePath = storage
                        .child("conversation_images")
                        .child(conversationId)
                        .child(imageId +".jpg" );
                uploadImage(imagePath, resultUri);
            }
        }
    }

    //Todo image compression
    private void uploadImage(StorageReference path, Uri uri) {
        final StorageReference imagePath = path;
        imagePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUri = uri.toString();
                        sendMessage(imageUri, true);
                    }
                });
            }
        });
    }
    
    private void sendMessage(String text, Boolean isImage) {
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
            messageMap.put("image", isImage);

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

    //Message options dialog
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
        Message message = messagesRecyclerAdapter.getMessages().get(messagePosition);
        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Messages")
                .document(message.getMessage_id())
                .update("deleted", true);

        message.setDeleted(true);
        firebaseFirestore
                .collection("Conversations/" + conversationId + "/Deleted_Messages")
                .document(message.getMessage_id())
                .set(message);
    }

    private void copyMessage(int messagePosition) {
        String text = messagesRecyclerAdapter.getMessages().get(messagePosition).getMessage();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text", text);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        clipboardManager.setPrimaryClip(clip);
    }
}
