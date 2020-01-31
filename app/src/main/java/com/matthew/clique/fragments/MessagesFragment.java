package com.matthew.clique.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.R;
import com.matthew.clique.adapters.ConversationsRecyclerAdapter;
import com.matthew.clique.adapters.MessagesRecyclerAdapter;
import com.matthew.clique.models.Conversation;
import com.matthew.clique.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    public MessagesFragment() {

    }

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String userId;
    private List<String> conversationIdList;

    List<Conversation> conversationList;
    ConversationsRecyclerAdapter conversationsRecyclerAdapter;
    RecyclerView conversationsRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);
        //todo access from cache first even when online
        userId = firebaseAuth.getUid();

        conversationList = new ArrayList<>();
        conversationsRecyclerAdapter = new ConversationsRecyclerAdapter(conversationList);
        conversationsRecyclerView = view.findViewById(R.id.recyclerViewMessages);
        conversationsRecyclerView.setHasFixedSize(true);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        conversationsRecyclerView.setAdapter(conversationsRecyclerAdapter);

        conversationIdList= new ArrayList<>();

        firebaseFirestore
                .collection("Conversations")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        firebaseFirestore
                                .collection("Users/" + userId + "/Friends")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null  && !queryDocumentSnapshots.isEmpty()) {
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                if (doc.get("conversation_id") != null) {
                                                    String conversationId = doc.get("conversation_id").toString();
                                                    conversationIdList.add(conversationId);
                                                }
                                            }

                                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                                String documentId = doc.getDocument().getId();
                                                for (String conversationId : conversationIdList) {
                                                    if (documentId.equals(conversationId)) {
                                                        Conversation conversation = doc.getDocument().toObject(Conversation.class);
                                                        conversationList.add(conversation);
                                                    }
                                                }
                                            }

                                            conversationsRecyclerAdapter.loadConversations(conversationList);

                                        }
                                    }
                                });

                    }
                });

        return view;
    }
}
