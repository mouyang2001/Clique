package com.matthew.clique.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.ConversationActivity;
import com.matthew.clique.R;
import com.matthew.clique.models.Conversation;
import com.matthew.clique.models.User;

import java.util.HashMap;
import java.util.List;

public class ConversationsRecyclerAdapter extends RecyclerView.Adapter<ConversationsRecyclerAdapter.ViewHolder> {

    private List<Conversation> conversationsList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

    public ConversationsRecyclerAdapter(List<Conversation> conversationsList) {
        this.conversationsList = conversationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list_item, parent, false);
        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        return new ConversationsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    //always works with current user
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        final Conversation conversation = conversationsList.get(position);
        final List<String> userList = conversation.getUsers();
        for (String user : userList) {
            if (!user.equals(userId)) {
                firebaseFirestore
                        .collection("Users")
                        .document(user)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    String firstName = document.get("first_name").toString();
                                    String lastName = document.get("last_name").toString();
                                    String conversationName = firstName + " " + lastName;
                                    holder.setData(conversationName);
                                }
                            }
                        });
            }
        }

        holder.conversationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToConversation(holder.conversationName, conversation.getConversation_id());
            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView conversationNameField;
        CardView conversationCardView;

        String conversationName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            conversationNameField = itemView.findViewById(R.id.textViewConversationListName);
            conversationCardView = itemView.findViewById(R.id.cardViewConversationList);
        }

        public void setData(String conversationName) {
            this.conversationName = conversationName;
            conversationNameField.setText(this.conversationName);
        }

    }

    private void sendToConversation(String friendName, String conversationId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("friend_name", friendName);
        intent.putExtra("conversation_id", conversationId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if (conversationsList != null) {
            return conversationsList.size();
        } else {
            return 0;
        }
    }
}
