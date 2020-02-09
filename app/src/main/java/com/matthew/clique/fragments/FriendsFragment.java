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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
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
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

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
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        final String id = doc.getDocument().getId();
                                        firebaseFirestore
                                                .collection("Users")
                                                .document(id)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        User user = documentSnapshot.toObject(User.class);
                                                        friendsList.add(user);
                                                        friendsRecyclerAdapter.notifyDataSetChanged();
                                                        //todo notify inserted so that you can see who added you!
                                                    }
                                                });
                                    }
                                }


                            }
                        }
                    }
                });

        return view;
    }
}


