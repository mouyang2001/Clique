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

import com.matthew.clique.Toolkit;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton, signUpButton;
    private TextView loginWarning;
    private ProgressBar loginProgress;

    private FirebaseAuth firebaseAuth;

    private Toolkit toolkit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editTextLoginEmail);
        passwordField = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLoginAction);
        signUpButton = findViewById(R.id.buttonLoginSignUp);
        loginWarning = findViewById(R.id.textViewLoginWarning);
        loginProgress = findViewById(R.id.progressBarLogin);

        toolkit = new Toolkit(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAccount(v);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister(v);
            }
        });
    }

    private void loginAccount(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        toolkit.closeKeyboard();
        loginWarning.setText("");
        if (!email.isEmpty() && !password.isEmpty()) {
            loginProgress.setVisibility(View.VISIBLE);
            firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendToMain();
                            } else {
                                loginWarning.setText(getString(R.string.error_login));
                                loginProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        } else {
            loginWarning.setText(getString(R.string.warning_empty));
        }
    }

    private void sendToRegister(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("EXTRA_EMAIL", email);
        intent.putExtra("EXTRA_PASSWORD", password);
        startActivity(intent);
    }

    public void sendToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
