package com.ebel_frank.learnphysics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebel_frank.learnphysics.adapter.DBAdapter;

import java.util.Random;

import static com.ebel_frank.learnphysics.ScoreActivity.scoreIntent;
import static com.ebel_frank.learnphysics.TopicActivity.SUBJECT;

public class QuizActivity extends AppCompatActivity {

    public static final String DB_NAME = "com.ebel_frank.learnphysics.db_name";
    private static final String NUMBER_OF_QUESTION = "com.ebel_frank.learnphysics.questionNumber";

    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMilliseconds;
    private TextView question, option1, option2, option3, option4, testTimer, answeredQuestion;
    private String userOption = null;
    private Button submitBtn;
    private LinearLayout[] optionsLayout;
    private short mCurrentIndex = 0, layoutClicked = 0, score = 10, click = 0;
    private int[] numb;

    private final Random random = new Random();
    private DBAdapter dbHelper;
    private Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        TextView title = findViewById(R.id.title);
        title.setText(getIntent().getStringExtra(SUBJECT));
        answeredQuestion = findViewById(R.id.answered_questions);
        answeredQuestion.setText((mCurrentIndex+1)+"/10");
        question = findViewById(R.id.question);
        option1 = findViewById(R.id.optionText1);
        option2 = findViewById(R.id.optionText2);
        option3 = findViewById(R.id.optionText3);
        option4 = findViewById(R.id.optionText4);
        optionsLayout = new LinearLayout[]{
                findViewById(R.id.option1Layout),
                findViewById(R.id.option2Layout),
                findViewById(R.id.option3Layout),
                findViewById(R.id.option4Layout)
        };

        // gets the table name of the selected test and queries the database
        // using the table name then display the question to the user.
        dbHelper = DBAdapter.getHelper(this);  // instance of the database class.
        c  = dbHelper.query(getIntent().getStringExtra(DB_NAME));

        numb = new int[10];
        for(int i=0; i<numb.length; i++) {
            int num = random.nextInt(getIntent().getIntExtra(NUMBER_OF_QUESTION, 0));
            numb[i] = num;
        }
        loadQuestion(c.moveToPosition(numb[mCurrentIndex]));

        mTimeLeftInMilliseconds = 40000;  // 40 sec
        testTimer = findViewById(R.id.timer);
        startTimer();

        submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click==0) {
                    if (userOption == null) {
                        Toast.makeText(QuizActivity.this, "Select an option", Toast.LENGTH_SHORT).show();
                    } else {
                        submit();
                    }
                } else {
                    click = 0;
                    if (mCurrentIndex == 10) {
                        startActivity(scoreIntent(QuizActivity.this, getIntent().getStringExtra(SUBJECT), getIntent().getStringExtra(DB_NAME), score));
                        finish();
                    } else {
                        submitBtn.setText(R.string.confirm);
                        next();
                    }
                }
            }
        });
    }

    private void loadQuestion(Boolean check) {
        if (check) {
            question.setText(Html.fromHtml(c.getString(1)));
            option1.setText(Html.fromHtml(c.getString(2)));
            option2.setText(Html.fromHtml(c.getString(3)));
            option3.setText(Html.fromHtml(c.getString(4)));
            option4.setText(Html.fromHtml(c.getString(5)));
        }
        mCurrentIndex += 1;
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(final long millisUntilFinished) {
                mTimeLeftInMilliseconds = millisUntilFinished;

                short seconds = (short) (mTimeLeftInMilliseconds % 60000 / 1000);
                testTimer.setText(String.format("%d", seconds));
            }

            @Override
            public void onFinish() {
                submit();
            }
        }.start();
    }
    private void stopTimer() {
        mCountDownTimer.cancel();
    }

    public void clickedOption(View view) {
        clearCheck();
        switch (view.getId()) {
            case R.id.option1Layout:
                userOption = option1.getText().toString();
                layoutClicked = 0;
                break;
            case R.id.option2Layout:
                userOption = option2.getText().toString();
                layoutClicked = 1;
                break;
            case R.id.option3Layout:
                userOption = option3.getText().toString();
                layoutClicked = 2;
                break;
            case R.id.option4Layout:
                userOption = option4.getText().toString();
                layoutClicked = 3;
                break;
        }
        view.setBackground(getResources().getDrawable(R.drawable.stroke_shape_blue, null));
    }

    private void clearCheck(){
        optionsLayout[0].setBackground(getResources().getDrawable(android.R.color.transparent, null));
        optionsLayout[1].setBackground(getResources().getDrawable(android.R.color.transparent, null));
        optionsLayout[2].setBackground(getResources().getDrawable(android.R.color.transparent, null));
        optionsLayout[3].setBackground(getResources().getDrawable(android.R.color.transparent, null));
    }

    private void next() {
        clearCheck();
        answeredQuestion.setText((mCurrentIndex+1)+"/10");
        mTimeLeftInMilliseconds = 30000;  // 30 sec
        startTimer();
        for (LinearLayout linearLayout : optionsLayout) {
            linearLayout.setClickable(true);
        }
        loadQuestion(c.moveToPosition(numb[mCurrentIndex]));
    }

    private void submit() {
        click = 1;
        clearCheck();
        stopTimer();
        if(userOption == null) {
            score -= 1;
        }
        if(userOption!=null && !userOption.equals(c.getString(6))){
            optionsLayout[layoutClicked].setBackground(getResources().getDrawable(R.drawable.stroke_shape_red, null));
            score -= 1;
        }
        for (int i=0; i < optionsLayout.length; i++) {
            if (c.getString(i+2).equals(c.getString(6))) {
                optionsLayout[i].setBackground(getResources().getDrawable(R.drawable.stroke_shape_green, null));
            }
            optionsLayout[i].setClickable(false);
        }
        userOption = null;
        if (mCurrentIndex == 10) submitBtn.setText(R.string.finish);
        else submitBtn.setText(R.string.next);
    }

    public static Intent quizIntent(Context context, String subject, String dbName, int questionNumber){
        Intent intent = new Intent(context, QuizActivity.class);
        intent.putExtra(SUBJECT, subject);
        intent.putExtra(DB_NAME, dbName);
        intent.putExtra(NUMBER_OF_QUESTION, questionNumber);
        return intent;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to exit?")
                .setMessage("If you exit the quiz, your progress will be lost.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        QuizActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startTimer();
        dbHelper.closeDataBase();
    }
}
