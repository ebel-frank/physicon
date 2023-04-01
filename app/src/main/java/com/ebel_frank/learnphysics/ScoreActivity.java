package com.ebel_frank.learnphysics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.ebel_frank.learnphysics.HomeActivity.share;
import static com.ebel_frank.learnphysics.QuizActivity.DB_NAME;
import static com.ebel_frank.learnphysics.QuizActivity.quizIntent;
import static com.ebel_frank.learnphysics.TopicActivity.SUBJECT;

public class ScoreActivity extends AppCompatActivity {

    private static final String EXTRA_TEST_SCORE = "com.ebeledike.myquiz.test_score";
    private TextView remark, score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        remark = findViewById(R.id.remark);
        int grade =  (getIntent().getIntExtra(EXTRA_TEST_SCORE, 0) * 100) / 10;
        score = findViewById(R.id.percentScore);
        score.setText(grade + "%");
        grades(grade);

    }

    private void grades(int grade){
        if(grade>=70) {
            remark.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            remark.setText("Excellent, an outstanding performance.");
        } else if (grade>=60)
            remark.setText("Good performance, keep it up.");
        else if(grade>=50)
            remark.setText("An average result, put more effort.");
        else if(grade>=45)
            remark.setText("Not too bad. Just a little more practice!");
        else {
            remark.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            remark.setText("Poor performance, you can do better. Practice makes perfect!.");
        }
    }

    public static Intent scoreIntent(Context packageContext, String subject, String dbName, int scores){
        Intent i = new Intent(packageContext, ScoreActivity.class);
        i.putExtra(EXTRA_TEST_SCORE, scores);
        i.putExtra(SUBJECT, subject);
        i.putExtra(DB_NAME, dbName);
        return i;
    }

    public void navigate(View view) {
        switch (view.getId()) {
            case R.id.restart:
                startActivity(quizIntent(ScoreActivity.this, getIntent().getStringExtra(SUBJECT), getIntent().getStringExtra(DB_NAME), 12));
                finish();
                break;
            case R.id.home:
                startActivity(new Intent(ScoreActivity.this, HomeActivity.class));
                finishAffinity();
                break;
            case R.id.share:
                startActivity(share(ScoreActivity.this));
                break;
        }
    }
}
