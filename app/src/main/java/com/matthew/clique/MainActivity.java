package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matthew.clique.fragments.FriendsFragment;
import com.matthew.clique.fragments.MessagesFragment;
import com.matthew.clique.fragments.ProfileFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    public String userName;
    public String userId;

    private Toolbar toolbar;
    private int menuResource = R.menu.messages_menu; //default

    private SwipeRefreshLayout swipeRefreshLayout;

    private MessagesFragment messagesFragment;
    private FriendsFragment friendsFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        bottomNavigation = findViewById(R.id.bottomNavigationViewMain);
        bottomNavigationControl();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutMain);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (!document.exists()) {
                                    sendTo(MainActivity.this, SetupActivity.class, true);
                                } else {
                                    //token id has to always update with the user's current phone
                                    firebaseAuth
                                            .getCurrentUser()
                                            .getIdToken(true)
                                            .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                                @Override
                                                public void onSuccess(GetTokenResult getTokenResult) {
                                                    String tokenId = getTokenResult.getToken();

                                                    Map<String, Object> tokenMap = new HashMap<>();
                                                    tokenMap.put("token_id", tokenId);

                                                    firebaseFirestore.collection("Users").document(userId).update(tokenMap);
                                                }
                                            });
                                }
                            } else {
                                String e = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

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
            case R.id.itemChangeBio:
                sendTo(MainActivity.this, BioActivity.class, false);
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
                        invalidateOptionsMenu();
                        break;
                    case R.id.bottomActionFriends:
                        replaceFragment(friendsFragment);
                        getSupportActionBar().setTitle(R.string.friends);
                        menuResource = R.menu.friends_menu;
                        invalidateOptionsMenu();
                        break;
                    case R.id.bottomActionProfile:
                        replaceFragment(profileFragment);
                        getSupportActionBar().setTitle(R.string.profile);
                        menuResource = R.menu.profile_menu;
                        invalidateOptionsMenu();
                        break;
                }

                return true;
            }
        });
    }

    private void initializeFragments() {
        messagesFragment = new MessagesFragment();
        friendsFragment = new FriendsFragment();
        profileFragment = new ProfileFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.frameLayoutMain, messagesFragment);
        fragmentTransaction.add(R.id.frameLayoutMain, friendsFragment);
        fragmentTransaction.add(R.id.frameLayoutMain, profileFragment);

        //hides other fragments making messages default
        fragmentTransaction.hide(friendsFragment);
        fragmentTransaction.hide(profileFragment);

        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == messagesFragment) {
            fragmentTransaction.hide(friendsFragment);
            fragmentTransaction.hide(profileFragment);
        }

        if (fragment == friendsFragment) {
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(profileFragment);
        }

        if (fragment == profileFragment) {
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(friendsFragment);
        }

        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();

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
