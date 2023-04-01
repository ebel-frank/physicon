package com.ebel_frank.learnphysics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebel_frank.learnphysics.adapter.DBAdapter;

public class BookmarkActivity extends AppCompatActivity {
    private GridLayout gridBookmarks;
    private LinearLayout noBookmark;

    private ImageView[] topic_image;
    private TextView[] topic_textView;
    private CardView[] cardView;

    private Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        gridBookmarks = findViewById(R.id.grid_bookmarks);
        noBookmark = findViewById(R.id.no_bookmark);

        cardView = new CardView[]{findViewById(R.id.topic_1), findViewById(R.id.topic_2), findViewById(R.id.topic_3),
                findViewById(R.id.topic_4), findViewById(R.id.topic_5), findViewById(R.id.topic_6)};
        topic_image = new ImageView[]{findViewById(R.id.topic_image_1), findViewById(R.id.topic_image_2), findViewById(R.id.topic_image_3),
                findViewById(R.id.topic_image_4), findViewById(R.id.topic_image_5), findViewById(R.id.topic_image_6)};
        topic_textView = new TextView[]{findViewById(R.id.topic_title_1), findViewById(R.id.topic_title_2), findViewById(R.id.topic_title_3),
                findViewById(R.id.topic_title_4), findViewById(R.id.topic_title_5), findViewById(R.id.topic_title_6)};

        updateUI();
    }

    private void updateUI() {
        DBAdapter dbHelper = DBAdapter.getHelper(this);  // instance of the database class.
        c  = dbHelper.query("Bookmarks");

        int currentIndex = 0;
        CardView mCardView;
        if (c.moveToFirst() && c.getString(1) != null) {
            noBookmark.setVisibility(View.GONE);
            gridBookmarks.setVisibility(View.VISIBLE);
            do {
                mCardView = cardView[currentIndex];
                mCardView.setVisibility(View.VISIBLE);
                mCardView.setId(c.getInt(0));
                topic_image[currentIndex].setImageResource(c.getInt(2));
                topic_textView[currentIndex].setText(c.getString(1));
                currentIndex++;
            } while (c.moveToNext());
        } else {
            noBookmark.setVisibility(View.VISIBLE);
            gridBookmarks.setVisibility(View.GONE);
        }
    }

    public void navigate(View view) {
        startActivity(HomeActivity.navigation(view, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
