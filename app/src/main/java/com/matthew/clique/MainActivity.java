package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matthew.clique.fragments.FriendsFragment;
import com.matthew.clique.fragments.MessagesFragment;
import com.matthew.clique.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar toolbar;
    private int menuResource = R.menu.messages_menu; //default

    private MessagesFragment messagesFragment;
    private FriendsFragment friendsFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            toolbar = findViewById(R.id.toolbarMain);
            setSupportActionBar(toolbar);

            bottomNavigation = findViewById(R.id.bottomNavigationViewMain);
            bottomNavigationControl();
        } else {
            sendTo(MainActivity.this, LoginActivity.class, true);
        }

    }

    @Override
    protected void onStart() { //always checking if user is signed in
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore
                    .collection("Users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().exists()) {
                                    sendTo(MainActivity.this, SetupActivity.class, true);
                                }
                            }
                        }
                    });
        } else {
            sendTo(MainActivity.this, LoginActivity.class, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menuResource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.itemNewMessage:
                sendTo(MainActivity.this, NewMessage.class, false);
                return true;
            case R.id.itemSignOut:
                sendTo(MainActivity.this, LoginActivity.class, true);
                return true;
            case R.id.itemAddFriend:
                sendTo(MainActivity.this, AddContactsActivity.class, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void bottomNavigationControl() {
        initializeFragments();
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottomActionMessages:
                        replaceFragment(messagesFragment);
                        getSupportActionBar().setTitle(R.string.messages);
                        menuResource = R.menu.messages_menu;
                        break;
                    case R.id.bottomActionFriends:
                        replaceFragment(friendsFragment);
                        getSupportActionBar().setTitle(R.string.friends);
                        menuResource = R.menu.friends_menu;
                        break;
                    case R.id.bottomActionProfile:
                        replaceFragment(profileFragment);
                        getSupportActionBar().setTitle(R.string.profile);
                        menuResource = R.menu.profile_menu;
                        break;
                }

                return true;
            }
        });
    }

    private void sendTo(Context context, Class activity, boolean finish) {
        Intent intent = new Intent(context, activity);
        startActivity(intent);
        if (finish) {finish();}
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State connectedState = NetworkInfo.State.CONNECTED;
        NetworkInfo.State mobileConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifiConnection = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        return (mobileConnection == connectedState || wifiConnection == connectedState);
    }
}
