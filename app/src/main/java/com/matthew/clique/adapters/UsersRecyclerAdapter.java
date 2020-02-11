package com.matthew.clique.adapters;

import android.content.Context;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.AddContactsActivity;
import com.matthew.clique.R;
import com.matthew.clique.models.User;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {

    private List<User> usersList;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;

    private String userFirstName, userLastName;

    public UsersRecyclerAdapter(List<User> usersList) {
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

        firebaseFirestore
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userFirstName = documentSnapshot.get("first_name").toString();
                        userLastName = documentSnapshot.get("last_name").toString();
                    }
                });

        return new UsersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    //always works with current user
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        final String friendId = usersList.get(position).getUser_id();
        final String firstName = usersList.get(position).getFirst_name();
        final String lastName = usersList.get(position).getLast_name();
        String profileImage = usersList.get(position).getProfile_image();
        String fullName = firstName + lastName;
        holder.setUserData(fullName, profileImage);

        firebaseFirestore
                .collection("Users/" + userId + "/Friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(friendId)) {
                                    holder.addButton.setImageDrawable(context.getDrawable(R.drawable.ic_check));
                                } else {
                                    holder.addButton.setImageDrawable(context.getDrawable(R.drawable.ic_add_accent));
                                }
                            }
                        } else {
                            holder.addButton.setImageDrawable(context.getDrawable(R.drawable.ic_add_accent));
                        }
                    }
                });

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FieldValue timestamp = FieldValue.serverTimestamp();

                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("user_id", userId);
                userMap.put("time_added", timestamp);
                userMap.put("friend_first_name", userFirstName);
                userMap.put("friend_last_name", userLastName);

                HashMap<String, Object> friendMap = new HashMap<>();
                friendMap.put("user_id", friendId);
                friendMap.put("time_added", timestamp);
                friendMap.put("friend_first_name", firstName);
                friendMap.put("friend_last_name", lastName);

                //user get friends info
                firebaseFirestore
                        .collection("Users/" + userId + "/Friends")
                        .document(friendId)
                        .set(friendMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    holder.addButton
                                            .setImageDrawable(context.getDrawable(R.drawable.ic_check));
                                }
                            }
                        });

                //friend gets users info
                firebaseFirestore
                        .collection("Users/" + friendId + "/Friends")
                        .document(userId)
                        .set(userMap);
            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameField;
        private ImageView addButton;
        private CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameField = itemView.findViewById(R.id.textViewFriendListName);
            addButton = itemView.findViewById(R.id.imageViewFriendListAddButton);
            profileImage = itemView.findViewById(R.id.circleImageViewFriendList);
        }

        public void setUserData(String name, String profileUri) {
            nameField.setText(name);
            if (profileUri != null) {
                Glide.with(context).load(profileUri).into(profileImage);
            }
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
