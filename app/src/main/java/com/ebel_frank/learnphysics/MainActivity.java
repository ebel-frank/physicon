package com.ebel_frank.learnphysics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView splash_anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // makes the splash screen a fullscreen
        setContentView(R.layout.activity_main);

        splash_anim = findViewById(R.id.splash_anim);
        splash_anim.animate().translationY(800).setDuration(1600).setStartDelay(500);


        // 3.5 sec
        int SPLASH_TIME_OUT = 3500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
