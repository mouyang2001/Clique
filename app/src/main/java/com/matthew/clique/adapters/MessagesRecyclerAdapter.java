package com.matthew.clique.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matthew.clique.R;
import com.matthew.clique.fragments.MessageOptionsDialog;
import com.matthew.clique.models.Message;

import java.util.List;

public class MessagesRecyclerAdapter
        extends RecyclerView.Adapter<MessagesRecyclerAdapter.ViewHolder> {

    private List<Message> messageList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

    private MessageOptionsDialog messageOptionsDialog;

    public MessagesRecyclerAdapter(List<Message> messageList) { this.messageList = messageList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        return new MessagesRecyclerAdapter.ViewHolder(view);
    }

    public void refreshData(List<Message> list) {
        this.messageList = list;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyItemInserted(this.messageList.size());
    }

    public void deleteMessage(Message message) {
        if (message != null) {
            int position = this.messageList.indexOf(message);
            this.messageList.get(position).setDeleted(true);
        }
    }

    public List<Message> getMessages() {
        return this.messageList;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        holder.messageFieldUser.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition));

        Message message = this.messageList.get(position);

        String text = message.getMessage();
        String senderId = message.getSender();
        Boolean deleted = message.getDeleted();
        holder.setMessage(text, senderId, deleted);

        firebaseFirestore
                .collection("Users")
                .document(senderId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String profileUri = task.getResult().get("profile_image").toString();
                            holder.setProfileImage(profileUri);
                        }
                    }
                });

        holder.messageField.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadMessageOptions(position);
                return true;
            }
        });

        holder.messageFieldUser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadMessageOptions(position);
                return true;
            }
        });
    }

    private void loadMessageOptions(int position) {
        messageOptionsDialog = new MessageOptionsDialog(position);
        messageOptionsDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "messageOptions");

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

        public void setMessage(String text, String senderId, Boolean deleted) {
            if (senderId.equals(userId)) {
                profileImage.setVisibility(View.INVISIBLE);
                messageField.setVisibility(View.INVISIBLE);
                messageFieldUser.setVisibility(View.VISIBLE);

                if (deleted) {
                    messageDeleted(messageFieldUser);
                } else {
                    messageFieldUser.setText(text);
                }
            } else {
                if (deleted) {
                    messageDeleted(messageField);
                } else {
                    messageField.setText(R.string.message_deleted);
                }
            }
        }

        private void messageDeleted(TextView mf) {
            mf.setBackgroundResource(R.drawable.bg_message_deleted);
            mf.setText(R.string.message_deleted);
        }

        public void setProfileImage(String profileUri) {
            if (profileUri != null) {
                Glide.with(context).load(profileUri).into(profileImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (this.messageList != null) {
            return this.messageList.size();
        } else {
            return 0;
        }
    }
}
