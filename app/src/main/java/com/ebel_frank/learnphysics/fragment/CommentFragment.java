package com.ebel_frank.learnphysics.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ebel_frank.learnphysics.LoginActivity;
import com.ebel_frank.learnphysics.R;
import com.ebel_frank.learnphysics.model.Comment;
import com.ebel_frank.learnphysics.model.User;
import com.ebel_frank.learnphysics.notification.APIService;
import com.ebel_frank.learnphysics.notification.Client;
import com.ebel_frank.learnphysics.notification.Data;
import com.ebel_frank.learnphysics.notification.Response;
import com.ebel_frank.learnphysics.notification.Sender;
import com.ebel_frank.learnphysics.notification.Token;
import com.ebel_frank.learnphysics.utils.PictureUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.ebel_frank.learnphysics.AskQuestionActivity.IMAGE_REQUEST_CODE;
import static com.ebel_frank.learnphysics.AskQuestionActivity.formatTime;
import static com.ebel_frank.learnphysics.HomeActivity.USER_BG_COLOR;
import static com.ebel_frank.learnphysics.HomeActivity.USER_NAME;
import static com.ebel_frank.learnphysics.SettingsActivity.PREFS;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.AUTHOR_ID;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.DISCUSS_ID;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.AUTHOR_QUESTION;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.QUESTION_IMAGE_URL;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.TIMESTAMP;
import static com.ebel_frank.learnphysics.fragment.DiscussionFragment.totalComment;

public class CommentFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaterialLetterIcon author_profile_image;
    private TextView authorUsername, authorQuestion, userAnswered, pastTime;
    private File mPhotoFile;
    private ImageView btn_send, questionImage, add_photo, user_imageView, cancel;
    private PhotoView imagePhoto;
    private Uri fileUri;
    private String fileUrl = "default";
    private EditText text_send;
    private RelativeLayout image_layout;

    private ProgressDialog progressDialog;
    private List<Comment> mComments;
    private CommentAdapter commentAdapter;

    private FirebaseUser fUser;
    private DatabaseReference reference;
    private static SharedPreferences preference;

    private APIService apiService;
    private boolean notify = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        imagePhoto = new PhotoView(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        final Bundle bundle = getArguments();
        preference = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE);
        author_profile_image = view.findViewById(R.id.author_profile_image);
        author_profile_image.setLetter(bundle.getString(USER_NAME).equals("You") ? preference.getString(USER_NAME, null):bundle.getString(USER_NAME));
        author_profile_image.setShapeColor(bundle.getInt(USER_BG_COLOR));

        authorUsername = view.findViewById(R.id.author_username);
        authorUsername.setText(bundle.getString(USER_NAME));

        authorQuestion = view.findViewById(R.id.question);
        authorQuestion.setText(bundle.getString(AUTHOR_QUESTION));

        questionImage = view.findViewById(R.id.questionImage);
        if (!bundle.getString(QUESTION_IMAGE_URL).equals("default")) {
            mPhotoFile = PictureUtils.getPhotoFile(getContext(), bundle.getString(DISCUSS_ID));
            if (!mPhotoFile.exists()) {
                Glide.with(getActivity())
                        .load(bundle.getString(QUESTION_IMAGE_URL))
                        .into(questionImage);
                new PictureUtils.Download(mPhotoFile).execute(bundle.getString(QUESTION_IMAGE_URL));
            } else {
                questionImage.setImageURI(Uri.fromFile(mPhotoFile));
            }
            questionImage.setVisibility(View.VISIBLE);
        }
        questionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePhoto.setImageBitmap(PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity()));
                if (imagePhoto.getParent() != null) {
                    ((ViewGroup) imagePhoto.getParent()).removeView(imagePhoto);
                }
                displayDialog(imagePhoto).show();
            }
        });

        userAnswered = view.findViewById(R.id.user_answered);
        totalComment(bundle.getString(DISCUSS_ID), userAnswered);

        pastTime = view.findViewById(R.id.past_time);
        pastTime.setText(bundle.getString(TIMESTAMP));

        recyclerView = view.findViewById(R.id.fragment_comment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mComments = new ArrayList<>();
        readComments(bundle.getString(DISCUSS_ID));

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        text_send = view.findViewById(R.id.txt_message);

        image_layout = view.findViewById(R.id.image_layout);
        user_imageView = view.findViewById(R.id.user_imageView);

        add_photo = view.findViewById(R.id.add_photo);
        add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
            }
        });
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        btn_send = view.findViewById(R.id.send_btn);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = text_send.getText().toString().trim();
                if (!message.equals("")) {
                    sendMessage(bundle.getString(DISCUSS_ID), bundle.getString(AUTHOR_ID), fUser.getUid(), message);
                    if (fileUri != null) cancel();
                }else {
                    Toast.makeText(getContext(), "Enter your message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
        return view;
    }

    private Dialog displayDialog(PhotoView photo) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(photo)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void cancel() {
        int cx = image_layout.getWidth() / 2;
        int cy = image_layout.getHeight() / 2;
        float radius = image_layout.getWidth();
        Animator anim = ViewAnimationUtils
                .createCircularReveal(image_layout, cx, cy, radius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                image_layout.setVisibility(View.GONE);
                add_photo.setVisibility(View.VISIBLE);
            }
        });
        anim.start();
        fileUri = null;
        user_imageView.setImageURI(null);
    }

    private void readComments(String discussionId) {
        progressDialog = LoginActivity.progressDialog(getContext(), "Loading...");
        progressDialog.show();
        reference = FirebaseDatabase.getInstance().getReference("Comments").child(discussionId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mComments.clear();
                for (DataSnapshot snaps: snapshot.getChildren()) {
                    Comment comment = snaps.getValue(Comment.class);
                    mComments.add(comment);
                }
                commentAdapter = new CommentAdapter(mComments);
                recyclerView.setAdapter(commentAdapter);

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage(String discussionId, String receiver, String sender, String comment) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(discussionId).push();
        String commentId = reference.getKey();
        if (fileUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
            StorageReference filePath = storageReference.child(commentId +".jpg");

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
                        fileUrl = downloadUri.toString();
                        reference.setValue(new Comment(commentId, sender, comment, fileUrl));
                    }
                }
            });
        } else {
            reference.setValue(new Comment(commentId, sender, comment, fileUrl));
        }

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(sender);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify && !receiver.equals(user.get_id())) {
                    sendNotification(user.get_id(), receiver, user.getUsername());
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String senderId, String receiverId, final String sender) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snaps : snapshot.getChildren()) {
                    Token token = snaps.getValue(Token.class);
                    Data data = new Data(senderId, sender+" commented on your question", "New Message", receiverId, android.R.drawable.star_on);

                    Sender body = new Sender();
                    apiService.sendNotification(body)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    if (response.code() == 200 && response.body().success != 1) {
                                        Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void readUsers(String senderId, final MaterialLetterIcon profile_image, final TextView username) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(senderId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                String _name = user.getUsername();
                username.setText(user.get_id().equals(firebaseUser.getUid()) ? "You" : _name);
                profile_image.setLetter(_name);
                profile_image.setShapeColor(Integer.parseInt(user.getBgColor()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == IMAGE_REQUEST_CODE && data != null) {
            fileUri = data.getData();
            image_layout.setVisibility(View.VISIBLE);
            user_imageView.setImageURI(fileUri);
            add_photo.setVisibility(View.GONE);
        } else {
            Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private class CommentHolder extends RecyclerView.ViewHolder {
        final MaterialLetterIcon otherProfileImage;
        final TextView otherUsername;
        final TextView commentTextView;
        final TextView pastTime;
        final ImageView commentImageView;
        File photoFile;

        private CommentHolder(View itemView) {
            super(itemView);
            otherProfileImage = itemView.findViewById(R.id.other_profile_image);
            otherUsername = itemView.findViewById(R.id.other_username);
            commentImageView = itemView.findViewById(R.id.commentImageView);
            commentTextView = itemView.findViewById(R.id.comment);
            pastTime = itemView.findViewById(R.id.other_time);
        }

        private void bindCrime(Comment comment) {
            readUsers(comment.getSenderId(), otherProfileImage, otherUsername);
            if (!comment.getImageUrl().equals("default")) {
                photoFile = PictureUtils.getPhotoFile(getContext(), comment.getCommentId());
                commentImageView.setVisibility(View.VISIBLE);
                if (!photoFile.exists()) {
                    Glide.with(getContext())
                            .load(comment.getImageUrl())
                            .into(commentImageView);
                    new PictureUtils.Download(photoFile).execute(comment.getImageUrl());
                } else {
                    commentImageView.setImageURI(Uri.fromFile(photoFile));
                }
            }
            commentImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imagePhoto.setImageBitmap(PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity()));
                    if (imagePhoto.getParent() != null) {
                        ((ViewGroup) imagePhoto.getParent()).removeView(imagePhoto);
                    }
                    displayDialog(imagePhoto).show();
                }
            });
            commentTextView.setText(comment.getComment());
            pastTime.setText(formatTime(comment.getTimestamp()));
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {
        private final List<Comment> mComments;

        CommentAdapter(List<Comment> mComments) {
            this.mComments = mComments;
        }

        @NonNull
        @Override
        public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.comment_layout, parent, false);
            return new CommentHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
            holder.bindCrime(this.mComments.get(position));
        }

        @Override
        public int getItemCount() {
            return this.mComments.size();
        }
    }
}
