package com.matthew.clique;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

//import com.matthew.clique.LoginActivity; is how you import other classes

public class RegisterActivity extends AppCompatActivity {

    EditText emailField, passwordField, passwordConfirmField;
    Button createAccount;
    TextView registerWarning;
    ProgressBar registerProgress;

    Toolkit toolkit;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editTextRegisterEmail);
        passwordField = findViewById(R.id.editTextRegisterPassword);
        passwordConfirmField = findViewById(R.id.editTextRegisterPasswordConfirm);
        createAccount = findViewById(R.id.buttonRegister);
        registerWarning = findViewById(R.id.textViewRegisterWarning);
        registerProgress = findViewById(R.id.progressBarRegister);

        Intent intent = getIntent();
        String intentEmail = intent.getStringExtra("EXTRA_EMAIL");
        String intentPassword = intent.getStringExtra("EXTRA_PASSWORD");
        emailField.setText(intentEmail);
        passwordField.setText(intentPassword);

        toolkit = new Toolkit(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(v);
            }
        });
    }

    private void createAccount(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String passwordConfirm = passwordConfirmField.getText().toString();

        toolkit.closeKeyboard();

        registerWarning.setText("");
        if (!email.isEmpty() && !password.isEmpty() && !passwordConfirm.isEmpty()) {
            if (password.equals(passwordConfirm)) {
                registerProgress.setVisibility(View.VISIBLE);
                firebaseAuth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendToSetup();
                                } else {
                                    registerWarning.setText(getString(R.string.error_register));
                                    registerProgress.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
            } else {
                registerWarning.setText(getString(R.string.warning_match));
            }
        } else {
            registerWarning.setText(getString(R.string.warning_empty));
        }
    }

    public void sendToSetup() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
        finish();
    }
}
