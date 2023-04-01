package com.ebel_frank.learnphysics.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ebel_frank.learnphysics.AskQuestionActivity;
import com.ebel_frank.learnphysics.LoginActivity;
import com.ebel_frank.learnphysics.R;
import com.ebel_frank.learnphysics.model.Discussion;
import com.ebel_frank.learnphysics.notification.Token;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ebel_frank.learnphysics.AskQuestionActivity.formatTimeFrom;
import static com.ebel_frank.learnphysics.HomeActivity.USER_BG_COLOR;
import static com.ebel_frank.learnphysics.HomeActivity.USER_NAME;
import static com.ebel_frank.learnphysics.SettingsActivity.PREFS;

public class DiscussionFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MaterialLetterIcon profile_image;
    private TextView askQuestion;
    private ProgressDialog progressDialog;
    private List<Discussion> mDiscussions;
    static final String DISCUSS_ID = "com.ebel_frank.learnphysics.discussionId";
    static final String AUTHOR_ID = "com.ebel_frank.learnphysics.authorId";
    static final String TIMESTAMP = "com.ebel_frank.learnphysics.timestamp";
    static final String AUTHOR_QUESTION = "com.ebel_frank.learnphysics.authorQuestion";
    static final String QUESTION_IMAGE_URL = "com.ebel_frank.learnphysics.authorImageUrl";

    private DiscussAdapter discussAdapter;
    private static SharedPreferences preference;
    private FirebaseUser firebaseUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion, container, false);

        preference = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE);
        profile_image = view.findViewById(R.id.user_profile_image);
        profile_image.setLetter(preference.getString(USER_NAME, null));
        profile_image.setShapeColor(preference.getInt(USER_BG_COLOR, 0));
        askQuestion = view.findViewById(R.id.askQuestion);
        askQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AskQuestionActivity.class));
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.dark_turquoise),
                getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        readQuestions();
                    }
                }, 3000);
            }
        });

        recyclerView = view.findViewById(R.id.fragment_discussion_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDiscussions = new ArrayList<>();
        readQuestions();
        return view;
    }

    private void readQuestions() {
        progressDialog = LoginActivity.progressDialog(getContext(), "Loading...");
        progressDialog.show();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Discussion");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mDiscussions.clear();
                for (DataSnapshot snaps: snapshot.getChildren()) {
                    Discussion discussion = snaps.getValue(Discussion.class);
                    try {
                        if (discussion.getAuthor().get_id().equals(firebaseUser.getUid())) {
                            discussion.getAuthor().setUsername("You");
                            mDiscussions.add(0, discussion);
                            continue;
                        }
                        mDiscussions.add(0, discussion);
                    } catch (Exception ignored){}
                }

                discussAdapter = new DiscussAdapter(mDiscussions);
                recyclerView.setAdapter(discussAdapter);

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    static void totalComment(String discussionId, final TextView answers) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments");
        reference.child(discussionId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int num = (int) snapshot.getChildrenCount();
                    answers.setText(num + " Answers");
                } else {
                    answers.setText("0 Answer");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void replaceFragment(Bundle bundle){
        Fragment nextFragment = new CommentFragment();
        nextFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, nextFragment)
                .addToBackStack(null)
                .commit();
        //Log.d("TAG", "reloadFragment Called");
    }

    private class DiscussHolder extends RecyclerView.ViewHolder {
        final MaterialLetterIcon authorProfileImage;
        final TextView authorUsername;
        final TextView authorQuestion;
        final TextView userAnswered;
        final TextView pastTime;
        private Bundle bundle;

        private DiscussHolder(View itemView) {
            super(itemView);
            authorProfileImage = itemView.findViewById(R.id.author_profile_image);
            authorUsername = itemView.findViewById(R.id.author_username);
            authorQuestion = itemView.findViewById(R.id.question);
            userAnswered = itemView.findViewById(R.id.user_answered);
            pastTime = itemView.findViewById(R.id.past_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(bundle);
                }
            });
        }

        private void bindCrime(Discussion discussion) {
            String _name, timestamp, question;
            _name = discussion.getAuthor().getUsername();
            int bg_color = Integer.parseInt(discussion.getAuthor().getBgColor());
            timestamp = formatTimeFrom(discussion.getTimestamp());
            question = discussion.getQuestion();
            authorProfileImage.setLetter(_name.equals("You") ? preference.getString(USER_NAME, null):_name);
            authorProfileImage.setShapeColor(bg_color);
            authorUsername.setText(_name);
            authorQuestion.setText(question);
            totalComment(discussion.getDiscussionId(), userAnswered);
            pastTime.setText(timestamp);
            bundle = new Bundle();
            bundle.putString(USER_NAME, _name);
            bundle.putString(AUTHOR_ID, discussion.getAuthor().get_id());
            bundle.putInt(USER_BG_COLOR, bg_color);
            bundle.putString(AUTHOR_QUESTION, question);
            bundle.putString(QUESTION_IMAGE_URL, discussion.getImageUrl());
            bundle.putString(DISCUSS_ID, discussion.getDiscussionId());
            bundle.putString(TIMESTAMP, timestamp);
        }
    }

    private class DiscussAdapter extends RecyclerView.Adapter<DiscussHolder> {
        private final List<Discussion> mDiscussions;

        DiscussAdapter(List<Discussion> mDiscussions) {
            this.mDiscussions = mDiscussions;
        }

        @NonNull
        @Override
        public DiscussHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.discussion_layout, parent, false);
            return new DiscussHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DiscussHolder holder, int position) {
            holder.bindCrime(mDiscussions.get(position));
        }

        @Override
        public int getItemCount() {
            return this.mDiscussions.size();
        }
    }
}
