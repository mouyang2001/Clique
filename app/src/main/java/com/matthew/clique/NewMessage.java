package com.matthew.clique;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewMessage extends AppCompatActivity {

    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private RecyclerView friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        toolbar = findViewById(R.id.toolbarNewMessage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.new_message);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        friendsList = findViewById(R.id.recyclerViewNewMessage);

    }
}
