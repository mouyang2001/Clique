package com.matthew.clique.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
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
        Message message = this.messageList.get(position);

        String senderId = message.getSender();
        holder.setMessage(message);

        holder.animation(senderId);

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
//                holder.messageField.setAlpha(0.8f);
                return true;
            }
        });

        holder.messageFieldUser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadMessageOptions(position);
//                holder.messageFieldUser.setAlpha(0.8f);
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
        private ImageView profileImage, messagePhoto, userPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageField = itemView.findViewById(R.id.textViewMessageText);
            messageFieldUser = itemView.findViewById(R.id.textViewMessageTextUser);
            profileImage = itemView.findViewById(R.id.circleImageViewMessage);
            messagePhoto = itemView.findViewById(R.id.imageViewMessagePhoto);
            userPhoto = itemView.findViewById(R.id.imageViewMessagePhotoUser);
        }

        public void setMessage(Message message) {
            if (message.getSender().equals(userId)) {
                profileImage.setVisibility(View.INVISIBLE);
                messageField.setVisibility(View.INVISIBLE);
                messageFieldUser.setVisibility(View.VISIBLE);

                messageSet(messageFieldUser, message);
            } else {
                messageSet(messageField, message);
            }
        }

        private void messageSet(TextView textField, Message message) {
            if (message.getDeleted()) {
                textField.setBackgroundResource(R.drawable.bg_message_deleted);
                textField.setText(R.string.message_deleted);
            } else {
                if (message.getImage()) {
                    textField.setVisibility(View.INVISIBLE);
                    if (message.getSender().equals(userId)) {
                        userPhoto.setVisibility(View.VISIBLE);
                        Glide.with(context).load(message.message)
                                .override(200, 200).into(userPhoto);
                    } else {
                        messagePhoto.setVisibility(View.VISIBLE);
                        Glide.with(context).load(message.message)
                                .override(200, 200).into(messagePhoto);
                    }
                } else {
                    textField.setText(message.getMessage());
                }
            }
        }

        public void setProfileImage(String profileUri) {
            if (profileUri != null) {
                Glide.with(context).load(profileUri).into(profileImage);
            }
        }

        private void animation(String senderId) {
            if (senderId.equals(userId)) {
                messageFieldUser.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition));
            } else {
                messageField.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_transition_left));
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
