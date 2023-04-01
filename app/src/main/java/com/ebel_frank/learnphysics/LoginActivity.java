package com.ebel_frank.learnphysics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        // checks if user is logged in or not
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        TextView forgotPass = findViewById(R.id.forgot_password);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View forgotView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.forgot_layout, null);
                AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
                        .setView(forgotView)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                ProgressBar progressBar = forgotView.findViewById(R.id.progressBar);
                EditText forgottenEmail = forgotView.findViewById(R.id.forgotten_email);
                Button continueBtn =forgotView.findViewById(R.id.continueBtn);

                forgottenEmail.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // This space is intentionally left blank
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) continueBtn.setEnabled(true);
                        else continueBtn.setEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // This space is intentionally left blank
                    }
                });
                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseAuth.sendPasswordResetEmail(forgottenEmail.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.cancel();
                                            Toast.makeText(LoginActivity.this, "Please check your email to continue", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                    }
                });
                dialog.show();
            }
        });

        Button login = findViewById(R.id.login_btn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_txt = email.getText().toString();
                String password_txt = password.getText().toString();
                if(email_txt.isEmpty()) email.setError("Required");
                if(password_txt.isEmpty()) password.setError("Required");
                else{
                    progressDialog = progressDialog(LoginActivity.this, "Signing in");
                    progressDialog.show();
                    login(email_txt, password_txt);
                }
            }
        });

        FloatingActionButton register = findViewById(R.id.register_btn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }
    private void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (true/*auth.getCurrentUser().isEmailVerified()*/) {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify email address", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    public static ProgressDialog progressDialog(Context context, String message) {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage(message);
        progress.setCanceledOnTouchOutside(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progress;
    }
}
