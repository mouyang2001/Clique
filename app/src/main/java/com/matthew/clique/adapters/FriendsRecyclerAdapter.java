package com.matthew.clique.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.matthew.clique.ConversationActivity;
import com.matthew.clique.R;
import com.matthew.clique.Toolkit;
import com.matthew.clique.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.ViewHolder> {

    private List<User> usersList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

    private Toolkit tk;

    public FriendsRecyclerAdapter(List<User> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        tk = new Toolkit(null);

        return new FriendsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    //always works with current user
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        final String friendId = usersList.get(position).getUser_id();
        final String firstName = usersList.get(position).getFirst_name();
        final String lastName = usersList.get(position).getLast_name();
        final String fullName = firstName + " " + lastName;
        holder.setUserData(firstName, lastName);
        holder.messageButton.setImageDrawable(context.getDrawable(R.drawable.ic_message));
        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore
                        .collection("Users/" + userId + "/Friends")
                        .document(friendId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    String conversationId = documentSnapshot.getString("conversation_id");
                                    if (documentSnapshot.getString("conversation_id") == null) {
                                        String conversationUID = tk.generateUID(20);
                                        List<String> users = new ArrayList<>();
                                        users.add(userId);
                                        users.add(friendId);

                                        createConversation(conversationUID, users);
                                        sendToConversation(fullName, conversationUID);
                                    } else {
                                        sendToConversation(fullName, conversationId);
                                    }
                                }
                            }
                        });

            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameField;
        private ImageView messageButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameField = itemView.findViewById(R.id.textViewFriendListName);
            messageButton = itemView.findViewById(R.id.imageViewFriendListAddButton);
        }

        public void setUserData(String firstName, String lastName) {
            String name = firstName +" "+ lastName;
            nameField.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        if (usersList != null) {
            return usersList.size();
        } else {
            return 0;
        }
    }

    private void sendToConversation(String friendName, String conversationId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("friend_name", friendName);
        intent.putExtra("conversation_id", conversationId);
        context.startActivity(intent);
    }

    private void createConversation(String conversationUID, List<String> usersList) {
        FieldValue timestamp = FieldValue.serverTimestamp();
        String userId = usersList.get(0);
        String friendId = usersList.get(1);

        Map<String, Object> conversation = new HashMap<>();
        conversation.put("conversation_id", conversationUID);
        conversation.put("time_created", timestamp);
        conversation.put("users", usersList);

        Map<String, Object> conversationMeta = new HashMap<>();
        conversationMeta.put("conversation_id", conversationUID);

        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference userReference = firebaseFirestore
                .collection("Users/" + userId + "/Friends/")
                .document(friendId);
        batch.set(userReference, conversationMeta, SetOptions.merge());

        DocumentReference friendReference = firebaseFirestore
                .collection("Users/" + friendId + "/Friends/")
                .document(userId);
        batch.set(friendReference, conversationMeta, SetOptions.merge());

        DocumentReference conversationReference = firebaseFirestore
                .collection("Conversations")
                .document(conversationUID);
        batch.set(conversationReference, conversation);

        batch.commit();
    }
}
