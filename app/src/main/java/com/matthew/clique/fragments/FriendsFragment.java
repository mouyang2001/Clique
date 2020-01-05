package com.matthew.clique.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.R;
import com.matthew.clique.adapters.FriendsRecyclerAdapter;
import com.matthew.clique.adapters.UsersRecyclerAdapter;
import com.matthew.clique.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    Toolbar toolbar;

    private List<User> friendsList;
    private List<String> friendsListRaw;
    private String userId;

    private RecyclerView friendsRecyclerView;
    private FriendsRecyclerAdapter friendsRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public FriendsFragment() {
        //empty constructor required
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getUid();
        friendsListRaw = new ArrayList<>();

        friendsList = new ArrayList<>();
        friendsRecyclerAdapter = new FriendsRecyclerAdapter(friendsList);
        friendsRecyclerView = view.findViewById(R.id.recyclerViewFriends);
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsRecyclerView.setAdapter(friendsRecyclerAdapter);

        firebaseFirestore
                .collection("Users/" + userId + "/Friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                friendsListRaw.add(document.getId());
                            }

                            //this has to be nested inside
                            firebaseFirestore
                                    .collection("Users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    for (String userId : friendsListRaw) {
                                                        if (document.getId().equals(userId)) {
                                                            User user = document.toObject(User.class);
                                                            friendsList.add(user);
                                                            friendsRecyclerAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });

        return view;
    }
}


