package com.ebel_frank.learnphysics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebel_frank.learnphysics.adapter.DBAdapter;

import io.github.kexanie.library.MathView;

import static com.ebel_frank.learnphysics.QuizActivity.DB_NAME;
import static com.ebel_frank.learnphysics.SettingsActivity.PREFS;
import static com.ebel_frank.learnphysics.adapter.DBAdapter.EventDBHelper.TOPIC_IMAGE;

public class TopicActivity extends AppCompatActivity {

    private LinearLayout learnLayout;
    private byte click = 0;
    private static SharedPreferences preference;

    private DBAdapter dbHelper;
    private Cursor c;
    private static final String TOPIC_ID = "com.ebel_frank.learnphysics.topic_id";
    public static final String SUBJECT = "com.ebel_frank.learnphysics.subject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        getSupportActionBar().setTitle(getIntent().getIntExtra(SUBJECT, 0));

        dbHelper = DBAdapter.getHelper(this);  // instance of the database class.
        c  = dbHelper.query(getIntent().getStringExtra(DB_NAME));

        learnLayout = findViewById(R.id.learnLayout);
        updateUI();

        ProgressBar progressBar = findViewById(R.id.progressBar);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 5500); // 5500 means wait for 5.5 seconds
    }

    private void updateUI() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,30,0,10);

        TextView title, examplesTitle, solutionTitle;
        MathView explanation, examples, solution;
        ImageView image;
        int color = getResources().getColor(android.R.color.black), count = 1;
        c.moveToFirst();
        do {
            if (c.getString(1) != null) {
                title = new TextView(this, null);
                title.setText(c.getString(1));
                title.setTypeface(null, Typeface.BOLD);
                title.setTextSize(15);
                title.setLayoutParams(params);
                title.setTextColor(color);
                learnLayout.addView(title);
            }
            if (c.getString(2) != null) {
                explanation = new MathView(this, null);
                explanation.setText(c.getString(2));
                learnLayout.addView(explanation);
            }


            if (c.getBlob(3) != null) {
                image = new ImageView(this);
                byte[] data = c.getBlob(3);
                image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                image.setLayoutParams(layoutParams);
                learnLayout.addView(image);
            }

            if (c.getString(4) != null) {
                examplesTitle = new TextView(this);
                examplesTitle.setText("Example "+count);
                examplesTitle.setTypeface(null, Typeface.BOLD);
                examplesTitle.setTextSize(15);
                examplesTitle.setTextColor(color);
                examplesTitle.setLayoutParams(params);
                learnLayout.addView(examplesTitle);

                examples = new MathView(this, null);
                examples.setText(c.getString(4));
                learnLayout.addView(examples);
                count++;
            }

            if (c.getString(5) != null) {
                solutionTitle = new TextView(this);
                solutionTitle.setText("Solution");
                solutionTitle.setTypeface(null, Typeface.BOLD);
                solutionTitle.setTextSize(15);
                solutionTitle.setTextColor(color);
                solutionTitle.setLayoutParams(params);
                learnLayout.addView(solutionTitle);

                solution = new MathView(this, null);
                solution.setText(c.getString(5));
                learnLayout.addView(solution);
            }
        } while (c.moveToNext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmark_menu, menu);

        preference = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (preference.getInt(getResources().getString(getIntent().getIntExtra(SUBJECT, 0)), 0) == 1) {
            click = 1;
            menu.getItem(0).setIcon(R.drawable.bookmark);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.topic_bookmark) {
            if (click == 0) {
                click = 1;
                item.setIcon(R.drawable.bookmark);
                dbHelper.addBookmarkToDB(getIntent().getIntExtra(TOPIC_ID, 0),
                        getResources().getString(getIntent().getIntExtra(SUBJECT, 0)),
                        getIntent().getIntExtra(TOPIC_IMAGE, 0));
            } else {
                click = 0;
                item.setIcon(R.drawable.no_bookmark);
                dbHelper.deleteBookmarkFromDB(getIntent().getIntExtra(TOPIC_ID, 0));
            }
            SharedPreferences.Editor editor = preference.edit();
            editor.putInt(getResources().getString(getIntent().getIntExtra(SUBJECT, 0)), click);
            editor.apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent topicIntent(Context context, int topicImage, int topicId, int subject, String dbName){
        Intent intent = new Intent(context, TopicActivity.class);
        intent.putExtra(TOPIC_IMAGE, topicImage);
        intent.putExtra(TOPIC_ID, topicId);
        intent.putExtra(SUBJECT, subject);
        intent.putExtra(DB_NAME, dbName);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        c.close();
        dbHelper.closeDataBase();
    }
}
