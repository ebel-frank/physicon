package com.ebel_frank.learnphysics;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.firebase.auth.FirebaseAuth;

import static com.ebel_frank.learnphysics.HomeActivity.USER_BG_COLOR;
import static com.ebel_frank.learnphysics.HomeActivity.USER_EMAIL;
import static com.ebel_frank.learnphysics.HomeActivity.USER_NAME;


public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS = "com.ebel_frank.learnphysics_preferences";
    private static SharedPreferences preference;
    private TextView username;
    private TextView email;
    private MaterialLetterIcon profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        preference = getSharedPreferences(PREFS, MODE_PRIVATE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String _name = preference.getString(USER_NAME, null);
        username = findViewById(R.id.user_username);
        username.setText(_name);
        email = findViewById(R.id.user_email);
        email.setText(preference.getString(USER_EMAIL, null));
        profile_image = findViewById(R.id.user_profile_image);
        profile_image.setLetter(_name);
        profile_image.setShapeColor(preference.getInt(USER_BG_COLOR, 0));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        Preference reference, about, logout;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            reference = findPreference("reference");
            assert reference != null;
            reference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getContext(), ReferenceActivity.class));
                    return true;
                }
            });

            about = findPreference("about");
            assert about != null;
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getContext())
                            .setView(R.layout.about_dialog)
                            .create()
                            .show();
                    return true;
                }
            });

            logout = findPreference("sign_out");
            assert logout != null;
            logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Sign out")
                            .setMessage("Are you sure you want to sign out?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(getContext(), LoginActivity.class));
                                    getActivity().finishAffinity();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                            .show();
                    return true;
                }
            });
        }
    }
}