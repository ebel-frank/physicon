package com.ebel_frank.learnphysics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText username, email, password, password2;
    private TextView infoText;
    private Button register, login;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        infoText = findViewById(R.id.info_textView);

        login = findViewById(R.id.login_btn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        register = findViewById(R.id.register_btn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username_txt = username.getText().toString();
                String email_txt = email.getText().toString().trim();
                String password_txt = password.getText().toString();
                String password2_txt = password2.getText().toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && !Patterns.EMAIL_ADDRESS.matcher(email_txt).matches()) {
                    email.setError("Invalid Email Address.");
                }
                if(username_txt.isEmpty()) username.setError("Required");
                if(email_txt.isEmpty()) email.setError("Required");
                if(password_txt.isEmpty()) password.setError("Required");
                else if (!password_txt.equals(password2_txt)){
                    password2.setError("Enter similar password");
                }else if (password_txt.length()<6) password.setError("not less than 6 characters");
                else{
                    progressDialog = LoginActivity.progressDialog(RegisterActivity.this, "Signing up");
                    progressDialog.show();
                    register(username_txt, email_txt, password_txt);
                }
            }
        });
    }

    private void register(final String username, final String email, String password) {
        final int[] bgColors = {R.color.colorAccent, R.color.navy, R.color.dark_cyan, R.color.sienna,
                            R.color.colorPrimary, R.color.chocolate, R.color.dark_turquoise, R.color.fui_bgFacebook};
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Register Successful
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userId = firebaseUser.getUid();

                            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("_id", userId);
                            hashMap.put("username", username);
                            hashMap.put("email", email);
                            hashMap.put("bgColor", ""+bgColors[new Random().nextInt(8)]);

                            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        reference.setValue(hashMap);
                                        infoText.setVisibility(View.VISIBLE);
                                        infoText.setText(R.string.prompt_email_verify);
                                        progressDialog.cancel();
                                    } else {
                                        infoText.setVisibility(View.VISIBLE);
                                        infoText.setText(task.getException().getMessage());
                                        progressDialog.cancel();
                                    }
                                }
                            });
                        } else {
                            // Register failed
                            Log.d("TAG", "task: "+task.getException().getMessage());
                            progressDialog.cancel();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
