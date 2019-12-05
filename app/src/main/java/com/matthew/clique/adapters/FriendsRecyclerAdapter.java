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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.Conversation;
import com.matthew.clique.R;
import com.matthew.clique.models.User;

import java.util.HashMap;
import java.util.List;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.ViewHolder> {

    private List<User> usersList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

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

        return new FriendsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    //always works with current user
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        final String firstName = usersList.get(position).getFirst_name();
        final String lastName = usersList.get(position).getLast_name();
        holder.setUserData(firstName, lastName);
        holder.messageButton.setImageDrawable(context.getDrawable(R.drawable.ic_message));
        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Conversation.class);
                intent.putExtra("friend_name", firstName + " " +  lastName);
                context.startActivity(intent);
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
}
