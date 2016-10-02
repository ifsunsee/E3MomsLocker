// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class LockerSettingsActivity extends AppCompatActivity
        implements PatternViewDelegate {

    private Button recode_button_, submit_control_key_;
    private Switch use_locker_;
    private EditText control_key_number_, message_for_unlock_;
    private LockerPatternView pattern_view_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_settings);

        boolean prev_use_locker_value =  PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(String.valueOf(R.string.use_locker_screen),false);

        use_locker_ = (Switch)findViewById(R.id.use_locker);
        use_locker_.setChecked(prev_use_locker_value);
        if (prev_use_locker_value) {
            Intent intent = new Intent(getApplicationContext(), LockerControlService.class);
            startService(intent);
        }

        use_locker_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor =
                        PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(String.valueOf(R.string.use_locker_screen), isChecked);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), LockerControlService.class);
                if (isChecked) {
                    startService(intent);
                } else {
                    stopService(intent);
                }

            }
        });

        String prev_control_key_number = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(String.valueOf(R.string.control_key_number),"");
        control_key_number_ = (EditText)findViewById(R.id.control_key_number);
        control_key_number_.setText(prev_control_key_number);

        String prev_message_for_unlock = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(String.valueOf(R.string.message_for_unlock),"go to unlock");
        message_for_unlock_ = (EditText)findViewById(R.id.message_for_unlock);
        message_for_unlock_.setText(prev_message_for_unlock);

        submit_control_key_ = (Button)findViewById(R.id.submit_control_key);
        submit_control_key_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                SharedPreferences.Editor editor =
                        PreferenceManager
                                .getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString(
                        String.valueOf(R.string.control_key_number),
                        control_key_number_.getText().toString());
                editor.putString(
                        String.valueOf(R.string.message_for_unlock),
                        message_for_unlock_.getText().toString());
                editor.commit();
            }
        });

        recode_button_ = (Button)findViewById(R.id.recode_button);
        pattern_view_ = (LockerPatternView)findViewById(R.id.settings_pattern_view);

        recode_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                pattern_view_.startRecordPattern();
            }
        });

        pattern_view_.setDelegate(this);
    }

    public void onCreatedPattern(boolean successed, ArrayList<Integer> patterns) {
        if (successed) {
            String locker_key_pattern =  PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getString(String.valueOf(R.string.locker_key_pattern),"");

            if (locker_key_pattern.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Pattern key is empty!!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (locker_key_pattern.equals(patterns.toString())) {
                Toast.makeText(getApplicationContext(), "Pattern is matched", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Pattern is not matched", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRecordedPattern(boolean successed, ArrayList<Integer> patterns) {
        if (successed) {
            Toast.makeText(getApplicationContext(), "New Pattern is created " + patterns.toString(), Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor =
                    PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString(String.valueOf(R.string.locker_key_pattern), patterns.toString());
            editor.commit();
        }
    }

    public void onSelectedColor(boolean selected, int color) {
    }

}