package com.matthew.clique.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matthew.clique.FullscreenImage;
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

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyItemInserted(this.messageList.size());
    }

    public List<Message> getMessages() {
        return this.messageList;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final Message message = this.messageList.get(position);
        String senderId = message.getSender();

        if (message.getImage()) {
            holder.setPhoto(message);
        } else {
            holder.setMessage(message);
            holder.animate(senderId);
        }

        firebaseFirestore
                .collection("Users")
                .document(senderId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                String profileUri = task.getResult().get("profile_image").toString();
                                holder.setProfileImage(profileUri);
                            }

                        }
                    }
                });

        holder.photoReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToFullscreenActivity(message.getMessage(), message.getMessage_id());
            }
        });

        holder.photoSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToFullscreenActivity(message.getMessage(), message.getMessage_id());
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

    private void sendToFullscreenActivity(String uri, String id) {
        Intent intent = new Intent(context, FullscreenImage.class);
        intent.putExtra("uri", uri);
        intent.putExtra("image_id", id);
        context.startActivity(intent);
    }

    private void loadMessageOptions(int position) {
        messageOptionsDialog = new MessageOptionsDialog(position);
        messageOptionsDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "messageOptions");

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageField, messageFieldUser;
        private ImageView profileImage, photoSent, photoReceived;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageField = itemView.findViewById(R.id.textViewMessageText);
            messageFieldUser = itemView.findViewById(R.id.textViewMessageTextUser);
            profileImage = itemView.findViewById(R.id.circleImageViewMessage);
            photoReceived = itemView.findViewById(R.id.imageViewMessagePhoto);
            photoSent = itemView.findViewById(R.id.imageViewMessagePhotoUser);
        }

        private void setPhoto(Message message) {
            RequestOptions photoSettings = new RequestOptions().centerCrop().override(300, 300);
            if (message.getSender().equals(userId)) {
                profileImage.setVisibility(View.INVISIBLE);
                messageField.setVisibility(View.INVISIBLE);
                photoSent.setVisibility(View.VISIBLE);

                Glide.with(context).load(message.getMessage()).apply(photoSettings).into(photoSent);
            } else {
                photoReceived.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getMessage()).apply(photoSettings).into(photoReceived);
            }
        }

        private void setMessage(Message message) {
            String text = message.getMessage();
            if (message.getSender().equals(userId)) {
                profileImage.setVisibility(View.INVISIBLE);
                messageField.setVisibility(View.INVISIBLE);
                messageFieldUser.setVisibility(View.VISIBLE);

                messageFieldUser.setText(text);
            } else {
                messageField.setText(text);
            }
        }

        private void setProfileImage(String profileUri) {
            if (profileUri != null) {
                Glide.with(context).load(profileUri).into(profileImage);
            }
        }

        private void animate(String senderId) {
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
