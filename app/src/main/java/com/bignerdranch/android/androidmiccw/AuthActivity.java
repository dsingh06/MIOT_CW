package com.bignerdranch.android.androidmiccw;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.bignerdranch.android.androidmiccw.MainActivity.RESULT_FAIL;
import static com.bignerdranch.android.androidmiccw.MainActivity.RESULT_SUCCESS;

public class AuthActivity extends Activity {
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private EditText passwordInput;
    private Button signInBut;
    private Button registerBut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        signInBut = findViewById(R.id.email_sign_in_button);
        registerBut = findViewById(R.id.register_button);
        signInBut.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            signIn(email, password);
        });
        registerBut.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            createAccount(email, password);
        });
    }

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            updateUI(null);
                        }
                    }
                });
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent data = new Intent();
            String uid = user.getUid();
            data.putExtra("User ID", uid);
            setResult(RESULT_SUCCESS, data);
        } else {
            setResult(RESULT_FAIL);
        }
        finish();
    }
}
