package com.ebel_frank.learnphysics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ebel_frank.learnphysics.model.User;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.ebel_frank.learnphysics.SettingsActivity.PREFS;
import static com.ebel_frank.learnphysics.TopicActivity.topicIntent;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MaterialLetterIcon profile_image;
    private TextView username, email;
    private DrawerLayout drawer;
    public static final String USER_NAME = "com.ebel_frank.learnphysics.username";
    public static final String USER_EMAIL = "com.ebel_frank.learnphysics.email";
    public static final String USER_BG_COLOR = "com.ebel_frank.learnphysics.bg_color";
    private static SharedPreferences preference;
    private static SharedPreferences.Editor editor;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        preference = getSharedPreferences(PREFS, MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        profile_image = navigationView.getHeaderView(0).findViewById(R.id.user_profile_image);
        username = navigationView.getHeaderView(0).findViewById(R.id.user_username);
        email = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        navigationView.getHeaderView(0).findViewById(R.id.nav_topics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                editor = preference.edit();
                assert user != null;
                editor.putString(USER_NAME, user.getUsername());
                editor.putString(USER_EMAIL, user.getEmail());
                editor.putInt(USER_BG_COLOR, Integer.parseInt(user.getBgColor()));
                editor.apply();

                username.setText(user.getUsername());
                email.setText(user.getEmail());
                profile_image.setLetter(user.getUsername());
                profile_image.setShapeColor(Integer.parseInt(user.getBgColor()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        String _name = preference.getString(USER_NAME, null);
        username.setText(_name);
        email.setText(preference.getString(USER_EMAIL, null));
        profile_image.setLetter(_name);
        profile_image.setShapeColor(preference.getInt(USER_BG_COLOR, 0));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_bookmark:
                Intent bookmarkIntent = new Intent(this, BookmarkActivity.class);
                startActivity(bookmarkIntent);
                break;
            case R.id.nav_quiz:
                Intent quizIntent = new Intent(this, SetQuiz.class);
                startActivity(quizIntent);
                break;
            case R.id.nav_askAQuestion:
                Intent askAQuestionIntent = new Intent(this, AskQuestionActivity.class);
                startActivity(askAQuestionIntent);
                break;
            case R.id.nav_discussion:
                Intent discussionIntent = new Intent(this, DiscussionActivity.class);
                startActivity(discussionIntent);
                break;
            case R.id.nav_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.nav_share:
                Intent shareIntent = share(this);
                startActivity(shareIntent);
                break;
            case R.id.nav_contact:
                try {
                    startActivity(contact());
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public static Intent share(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String text = "\nHey,\nLet me recommend you this application, ";
        String link = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, text+link+"\n");
        return shareIntent;
    }

    private Intent contact () {
        Intent contactIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        contactIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ebelfrank.dev@gmail.com"});
        contactIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name)+" - contact");
        return contactIntent;

    }

    public static Intent navigation(View view, Context context) {
        Intent intent;
        switch (view.getId()) {
            case R.id.sound:
                intent = topicIntent(context, R.mipmap.sound,R.id.sound, R.string.sound, "SoundTopic");
                break;
            case R.id.elec_field:
                intent = topicIntent(context, R.mipmap.electric, R.id.elec_field, R.string.elec_field, "ElecTopic");
                break;
            case R.id.elec_magnetic:
                intent = topicIntent(context, R.mipmap.electromagnetic, R.id.elec_magnetic, R.string.elec_magnetic, "ElecMagTopic");
                break;
            case R.id.grav_field:
                intent = topicIntent(context, R.mipmap.gravity, R.id.grav_field, R.string.grav_field, "GravTopic");
                break;
            case R.id.electrolysis:
                intent = topicIntent(context, R.mipmap.electrolysis, R.id.electrolysis, R.string.electrolysis, "ElectroTopic");
                break;
            case R.id.ac_circuit:
                intent = topicIntent(context, R.mipmap.ac_icon, R.id.ac_circuit, R.string.ac_circuit, "ACTopic");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        return intent;
    }

    public void navigate(View view) {
        startActivity(navigation(view, this));
    }

}
