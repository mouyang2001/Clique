package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private Toolkit toolkit;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private EditText firstNameField, lastNameField;
    private Button setupButton;
    private ProgressBar progressBar;
    private TextView setupWarning;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        toolkit = new Toolkit(this);

        firstNameField = findViewById(R.id.editTextSetupFirstName);
        lastNameField = findViewById(R.id.editTextSetupLastName);
        setupButton = findViewById(R.id.buttonSetup);
        progressBar = findViewById(R.id.progressBarSetup);
        setupWarning = findViewById(R.id.textViewSetupWarning);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setup();
            }
        });

    }

    private void setup() {
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();

        toolkit.closeKeyboard();

        setupWarning.setText("");
        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("user_id", userId);
            userMap.put("first_name", firstName);
            userMap.put("last_name", lastName);
            userMap.put("updated", FieldValue.serverTimestamp());

            progressBar.setVisibility(View.VISIBLE);
            firebaseFirestore
                    .collection("Users")
                    .document(userId)
                    .set(userMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendToMain();
                            } else {
                                setupWarning.setText(getString(R.string.error_setup));
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        } else {
            setupWarning.setText(getString(R.string.warning_empty));
        }
    }

    public void sendToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
