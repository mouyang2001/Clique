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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
                String search = nameField.getText().toString().trim();
                userSearch(search);
            }
        });

        //Todo search users. Order by?

    }

    private void userSearch(String search) {
        firebaseFirestore
                .collection("Users")
                .orderBy("first_name")
                .startAt(search)
                .endAt(search + "\uf8ff")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        usersList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (!document.getId().equals(userId)) {
                                User user = document.toObject(User.class);
                                usersList.add(user);
                            }

                        }
                        usersRecyclerAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
