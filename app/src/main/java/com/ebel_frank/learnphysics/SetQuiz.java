package com.ebel_frank.learnphysics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import static com.ebel_frank.learnphysics.QuizActivity.quizIntent;

public class SetQuiz extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_quiz);

    }

    public void start_quiz(View view) {
        Toast toast = Toast.makeText(SetQuiz.this, "Good luck!!!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
        Context context = SetQuiz.this;
        switch (view.getId()) {
            case R.id.sound:
                startActivity(quizIntent(context, getResources().getString(R.string.sound), "SoundQuiz", 15));
                break;
            case R.id.elec_field:
                startActivity(quizIntent(context, getResources().getString(R.string.elec_field), "ElecQuiz", 26));
                break;
            case R.id.elec_magnetic:
                startActivity(quizIntent(context, "Electromagnetic Field", "ElecMagQuiz", 18));
                break;
            case R.id.grav_field:
                startActivity(quizIntent(context, "Gravitational Field", "GravQuiz", 12));
                break;
            case R.id.electrolysis:
                startActivity(quizIntent(context, getResources().getString(R.string.electrolysis), "ElectroQuiz", 12));
                break;
            case R.id.ac_circuit:
                startActivity(quizIntent(context, getResources().getString(R.string.ac_circuit), "ACQuiz", 17));
                break;
        }
    }
}
