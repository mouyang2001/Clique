package com.matthew.clique;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matthew.clique.adapters.UsersRecyclerAdapter;
import com.matthew.clique.models.User;

import java.util.ArrayList;
import java.util.List;

public class AddContactsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView userListView;
    private EditText nameField;
    private ImageView searchButton;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String userId;

    private RecyclerView usersRecyclerView;
    private UsersRecyclerAdapter usersRecyclerAdapter;
    private List<User> usersList;

    private Toolkit toolkit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        toolkit = new Toolkit(this);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();

        nameField = findViewById(R.id.editTextAddContacts);
        searchButton = findViewById(R.id.imageViewAddContactSearch);

        //toolbar
        toolbar = findViewById(R.id.toolbarAddFriends);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //recycler view and adapter
        usersList = new ArrayList<>();
        usersRecyclerAdapter = new UsersRecyclerAdapter(usersList);
        usersRecyclerView = findViewById(R.id.recyclerViewAddContacts);
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(usersRecyclerAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSearch();
            }
        });

        //Todo search users. Order by?

    }

    private void userSearch() {
        firebaseFirestore
                .collection("Users")
                .addSnapshotListener(AddContactsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                   if (!queryDocumentSnapshots.isEmpty()) {
                       for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                           if (doc.getType() == DocumentChange.Type.ADDED) {
                               String documentId = doc.getDocument().getId();

                               if (!documentId.equals(userId)) {
                                   User user = doc.getDocument().toObject(User.class);
                                   usersList.add(user);
                                   usersRecyclerAdapter.notifyDataSetChanged();
                               }

                           }
                       }
                   }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_friends_menu, menu);
        return true;
    }
}
