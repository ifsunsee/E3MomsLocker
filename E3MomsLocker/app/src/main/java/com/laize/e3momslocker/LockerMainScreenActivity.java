// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LockerMainScreenActivity extends AppCompatActivity
        implements PatternViewDelegate {
    private Button exit_button_;

    private AnalogClock analog_clock_;
    private LockerPatternView pattern_view_;
    private LockerScribbleView scrible_view_;
    private TextView mode_change_view_;
    private ImageView background_view_;
    private ImageView clear_all_button_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_main_screen);

        exit_button_ = (Button)findViewById(R.id.exit_button);
        exit_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just test code
                // finish();
            }
        });

        analog_clock_ = (AnalogClock)findViewById(R.id.analog_clock);
        pattern_view_ = (LockerPatternView)findViewById(R.id.pettern_view);
        pattern_view_.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        pattern_view_.setDelegate(this);

        scrible_view_ = (LockerScribbleView)findViewById(R.id.scribble_view);
        background_view_ = (ImageView)findViewById(R.id.background_view);

        mode_change_view_ = (TextView)findViewById(R.id.mode_change_view);
        mode_change_view_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analog_clock_.setVisibility(View.INVISIBLE);
                scrible_view_.setVisibility(View.VISIBLE);
                mode_change_view_.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams layout_params =
                        (RelativeLayout.LayoutParams)pattern_view_.getLayoutParams();
                layout_params.width = layout_params.width * 2 / 3;
                layout_params.height = layout_params.height * 2 / 3;
                layout_params.bottomMargin = layout_params.bottomMargin * 2 / 3;
                layout_params.rightMargin = layout_params.bottomMargin;
                layout_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                pattern_view_.setLayoutParams(layout_params);
                clear_all_button_.setVisibility(View.VISIBLE);
            }
        });

        clear_all_button_ = (ImageView)findViewById(R.id.clear_all_button);
        clear_all_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrible_view_.clearAllLines();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public void onCreatedPattern(boolean successed, ArrayList<Integer> patterns) {
        if (successed) {
            String locker_key_pattern =  PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getString(String.valueOf(R.string.locker_key_pattern),"");
            if (locker_key_pattern.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Unlocked by empty key pattern!!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            else if (locker_key_pattern.equals(patterns.toString())) {
                finish();
            }
        }
    }

    public void onRecordedPattern(boolean successed, ArrayList<Integer> patterns) {
    }

    public void onSelectedColor(boolean selected, int color) {
        if (!selected) {
            return;
        }
        if (scrible_view_.getVisibility() != View.VISIBLE) {
            return;
        }

        scrible_view_.setColor(color);
    }

    @Override
    protected void onUserLeaveHint() {
        finish();

        Intent intent = new Intent(this, LockerMainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

        super.onUserLeaveHint();
    }
}
