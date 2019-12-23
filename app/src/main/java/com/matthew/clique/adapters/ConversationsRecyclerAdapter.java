package com.matthew.clique.adapters;

import android.content.Context;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

        Conversation conversation = conversationsList.get(position);
        holder.setData(conversation.getConversation_id());

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView conversationNameField;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            conversationNameField = itemView.findViewById(R.id.textViewConversationListName);

        }

        public void setData(String conversationId) {
            conversationNameField.setText(conversationId);
        }

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
