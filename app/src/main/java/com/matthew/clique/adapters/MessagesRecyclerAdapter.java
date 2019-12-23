package com.matthew.clique.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matthew.clique.R;
import com.matthew.clique.Toolkit;
import com.matthew.clique.models.Message;

import java.util.List;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessagesRecyclerAdapter.ViewHolder> {

    private List<Message> messageList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

    private Toolkit tk;

    public MessagesRecyclerAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        tk = new Toolkit(null);

        return new MessagesRecyclerAdapter.ViewHolder(view);
    }

    @Override
    //always works with current user
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        Message message = messageList.get(position);

        String text = message.getMessage();
        String senderId = message.getSender();
        holder.setMessage(text, senderId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageField, messageFieldUser;
        private ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageField = itemView.findViewById(R.id.textViewMessageText);
            messageFieldUser = itemView.findViewById(R.id.textViewMessageTextUser);
            profileImage = itemView.findViewById(R.id.circleImageViewMessage);
        }

        public void setMessage(String text, String senderId) {
            if (senderId.equals(userId)) {
                profileImage.setVisibility(View.INVISIBLE);
                messageField.setVisibility(View.INVISIBLE);
                messageFieldUser.setVisibility(View.VISIBLE);
                messageFieldUser.setText(text);
            } else {
                messageField.setText(text);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        } else {
            return 0;
        }
    }
}
