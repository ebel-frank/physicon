package com.ebel_frank.learnphysics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ebel_frank.learnphysics.model.Discussion;
import com.ebel_frank.learnphysics.model.User;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.ebel_frank.learnphysics.HomeActivity.USER_BG_COLOR;
import static com.ebel_frank.learnphysics.HomeActivity.USER_NAME;
import static com.ebel_frank.learnphysics.SettingsActivity.PREFS;

public class AskQuestionActivity extends AppCompatActivity {

    private TextView num_text;
    private TextInputEditText user_question;
    private ImageView userImage, addPhoto;
    private ProgressDialog progressDialog;
    private User user;

    public static final int IMAGE_REQUEST_CODE = 300;
    private int clicked = 0;
    private Uri fileUri;
    private String fileUrl = "default";

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);

        progressDialog = LoginActivity.progressDialog(this, "Loading...");
        progressDialog.show();
        SharedPreferences preference = getSharedPreferences(PREFS, MODE_PRIVATE);
        String _name = preference.getString(USER_NAME, null);
        TextView username = findViewById(R.id.user_username);
        MaterialLetterIcon profile_image = findViewById(R.id.user_profile_image);
        username.setText(_name);
        profile_image.setLetter(_name);
        profile_image.setShapeColor(preference.getInt(USER_BG_COLOR, 0));

        num_text = findViewById(R.id.num_text);
        user_question = findViewById(R.id.user_question);
        user_question.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space is intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                num_text.setText(String.format("%d/1000", s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This space too was intentionally left blank
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userImage = findViewById(R.id.user_imageView);
        addPhoto = findViewById(R.id.add_photo);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked == 0) {
                    clicked = 1;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
                } else {
                    clicked = 0;
                    fileUri = null;
                    userImage.setImageURI(null);
                    userImage.setVisibility(View.GONE);
                    addPhoto.setImageResource(R.drawable.ic_add_image);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.post_question) {
            if (Objects.requireNonNull(user_question.getText()).toString().isEmpty()) {
                Toast.makeText(AskQuestionActivity.this, "You haven't entered any question", Toast.LENGTH_SHORT).show();
            } else {
                postQuestion(user, user_question.getText().toString().trim());
                startActivity(new Intent(this, DiscussionActivity.class));
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == IMAGE_REQUEST_CODE && data != null) {
            fileUri = data.getData();
            userImage.setVisibility(View.VISIBLE);
            userImage.setImageURI(fileUri);
            addPhoto.setImageResource(R.drawable.ic_action_cancel);
        } else {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void postQuestion(User author, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Discussion");

        String discussionId = reference.push().getKey();
        assert discussionId != null;
        if (fileUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
            StorageReference filePath = storageReference.child(discussionId +".jpg");

            StorageTask uploadTask = filePath.putFile(fileUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw  task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri =  task.getResult();
                        assert downloadUri != null;
                        fileUrl = downloadUri.toString();
                        reference.child(discussionId).setValue(new Discussion(discussionId, author, message, fileUrl));
                    }
                }
            });
        } else {
            reference.child(discussionId).setValue(new Discussion(discussionId, author, message, fileUrl));
        }
        Toast.makeText(AskQuestionActivity.this, "Posted", Toast.LENGTH_SHORT).show();
    }

    public static String formatTimeFrom(long timestamp) {
        DateFormat timeFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
        Date date = new Date();
        date.setTime(timestamp);
        return timeFormat.format(date);
    }

    public static String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS);
        return ago.toString();
    }
}
