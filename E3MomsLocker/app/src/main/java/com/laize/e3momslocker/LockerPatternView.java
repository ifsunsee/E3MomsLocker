// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO: document your custom view class.
 */
public class LockerPatternView extends View {
    final int DEFAULT_LINE_COLOR = 0xF0039433;
    final int DEFAULT_RECODE_MODE_BACKGROUND_COLOR = Color.argb(100,0,255,0);

    public class PatternKey {
        private RectF bounds_;
        private float round_;
        private RectF hit_bounds_;
        private float hit_round_;
        private int color_;
        private int id_;
        private Paint paint_;
        private Paint hit_paint_;

        public PatternKey(int id, int color, RectF bounds) {
            id_ = id;
            color_ = color;
            bounds_ = bounds;
            round_ = bounds_.width() < bounds_.height() ?
                    bounds_.width() * 0.4f : bounds_.height() * 0.4f;
            hit_bounds_ = new RectF(
                    bounds_.centerX() - bounds_.width() * 0.2f,
                    bounds_.centerY() - bounds_.height() * 0.2f,
                    bounds_.centerX() + bounds_.width() * 0.2f,
                    bounds_.centerY() + bounds_.height() * 0.2f);
            hit_round_ = hit_bounds_.width() < hit_bounds_.height() ?
                    hit_bounds_.width() * 0.4f : hit_bounds_.height() * 0.4f;

            paint_ = new Paint();
            paint_.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint_.setColor(color_);

            hit_paint_ = new Paint();
            hit_paint_.setFlags(Paint.ANTI_ALIAS_FLAG);
            hit_paint_.setColor(Color.LTGRAY);
        }

        public int getId() {
            return id_;
        }

        public int getColor() { return color_; }

        public float getHitRound() { return hit_round_; }

        public RectF getBounds() { return bounds_; }
        public RectF getHitBounds() { return hit_bounds_; }

        public void onDraw(Canvas canvas) {
            canvas.drawRoundRect(bounds_, round_, round_, paint_);
            canvas.drawRoundRect(hit_bounds_, hit_round_, round_, hit_paint_);
        }

        public boolean contains(PointF point) {
            return bounds_.contains(point.x, point.y);
        }

        public boolean hitTest(PointF point) {
            return hit_bounds_.contains(point.x, point.y);
        }
    }

    private Paint line_paint_;
    private ArrayList<PatternKey> pattern_keys_;
    private ArrayList<Integer> pattern_matches_;
    private boolean unlock_checking_;
    private PointF last_pos_;

    private PatternViewDelegate pattern_view_delegate_;
    private boolean recording_;

    public LockerPatternView(Context context) {
        super(context);
        init(null, 0);
    }

    public LockerPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LockerPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        line_paint_ = new Paint();
        line_paint_.setFlags(Paint.ANTI_ALIAS_FLAG);
        line_paint_.setColor(DEFAULT_LINE_COLOR);
        line_paint_.setTextAlign(Paint.Align.LEFT);
        line_paint_.setStrokeCap(Paint.Cap.ROUND);
        line_paint_.setStrokeJoin(Paint.Join.ROUND);

        this.setBackgroundColor(Color.TRANSPARENT);

        unlock_checking_ = false;

        pattern_keys_ = new ArrayList<PatternKey>();
        pattern_matches_ = new ArrayList<Integer>();

        recording_ = false;
    }

    private void initPatternKeys() {
        float interval_width = this.getWidth() / 17.0f;
        float interval_height = this.getWidth() / 17.0f;
        // 0 3 6
        // 1 4 7
        // 2 5 8
        pattern_keys_.clear();
        pattern_keys_.add(0, new PatternKey(0, Color.RED,
                new RectF(
                        interval_width * 0, interval_height * 0,
                        interval_width * 5, interval_height * 5)));
        pattern_keys_.add(1, new PatternKey(1, Color.GREEN,
                new RectF(
                        interval_width * 0, interval_height * 6,
                        interval_width * 5, interval_height * 11)));
        pattern_keys_.add(2, new PatternKey(2, Color.BLUE,
                new RectF(
                        interval_width * 0, interval_height * 12,
                        interval_width * 5, interval_height * 17)));

        pattern_keys_.add(3, new PatternKey(3, Color.YELLOW,
                new RectF(
                        interval_width * 6, interval_height * 0,
                        interval_width * 11, interval_height * 5)));
        pattern_keys_.add(4, new PatternKey(4, Color.MAGENTA,
                new RectF(
                        interval_width * 6, interval_height * 6,
                        interval_width * 11, interval_height * 11)));
        pattern_keys_.add(5, new PatternKey(5, Color.CYAN,
                new RectF(
                        interval_width * 6, interval_height * 12,
                        interval_width * 11, interval_height * 17)));

        pattern_keys_.add(6, new PatternKey(6, Color.BLACK,
                new RectF(
                        interval_width * 12, interval_height * 0,
                        interval_width * 17, interval_height * 5)));
        pattern_keys_.add(7, new PatternKey(7, Color.WHITE,
                new RectF(
                        interval_width * 12, interval_height * 6,
                        interval_width * 17, interval_height * 11)));
        pattern_keys_.add(8, new PatternKey(8, Color.GRAY,
                new RectF(
                        interval_width * 12, interval_height * 12,
                        interval_width * 17, interval_height * 17)));

        line_paint_.setStrokeWidth(pattern_keys_.get(0).getHitRound());
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initPatternKeys();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            last_pos_ = new PointF(event.getX(),event.getY());
            PatternKey key = getPatternKey(last_pos_);
            if (key == null) {
                return super.dispatchTouchEvent(event);
            }

            unlock_checking_ = true;
            pattern_matches_.clear();
            pattern_matches_.add(key.getId());
            this.invalidate();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            unlock_checking_ = false;
            last_pos_ = new PointF(event.getX(),event.getY());

            if (recording_) {
                recordedPattern();
            } else {
                createdPattern();

                PatternKey key = getPatternKey(last_pos_);
                if (key != null) {
                    selectedColor(key.getColor());
                }
            }

            this.invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE && unlock_checking_) {
            last_pos_ = new PointF(event.getX(),event.getY());
            PatternKey key = checkPatternKeyTest(last_pos_);
            if (key != null) {
                pattern_matches_.add(key.getId());
            }

            this.invalidate();
            return true;
        }

        return super.dispatchTouchEvent(event);
    }

    private PatternKey getPatternKey(PointF point) {
        for (Object object : pattern_keys_) {
            PatternKey key = (PatternKey)object;
            if (key.contains(point))
                return key;
        }

        return null;
    }

    private PatternKey checkPatternKeyTest(PointF point) {
        PatternKey key = getPatternKey(point);
        if (key == null)
            return null;

        if (!key.hitTest(point))
            return null;

        if (pattern_matches_.contains(key.getId()))
            return null;

        return key;
    }

    public void setDelegate(PatternViewDelegate delegate) {
        pattern_view_delegate_ = delegate;
    }

    public boolean startRecordPattern() {
        if (recording_)
            return false;

        recording_ = true;
        pattern_matches_.clear();
        this.setBackgroundColor(DEFAULT_RECODE_MODE_BACKGROUND_COLOR);
        this.invalidate();
        return true;
    }

    private void selectedColor(int color) {
        if (pattern_view_delegate_ != null) {
            pattern_view_delegate_.onSelectedColor(true, color);
        }
    }

    private void createdPattern() {
        if (pattern_view_delegate_ != null) {
            if (pattern_matches_.size() < 4) {
                pattern_view_delegate_.onCreatedPattern(false, pattern_matches_);
            } else {
                pattern_view_delegate_.onCreatedPattern(true, pattern_matches_);
            }
        }
    }

    private void recordedPattern() {
        if (pattern_view_delegate_ != null) {
            if (pattern_matches_.size() < 4) {
                pattern_view_delegate_.onRecordedPattern(false, pattern_matches_);
            } else {
                pattern_view_delegate_.onRecordedPattern(true, pattern_matches_);
            }
        }

        recording_ = false;
        this.setBackgroundColor(Color.TRANSPARENT);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Iterator iterator = pattern_keys_.iterator();
        while (iterator.hasNext()) {
            PatternKey pattern_key = (PatternKey)iterator.next();
            pattern_key.onDraw(canvas);
        }

        onPatternDraw(canvas);
    }

    protected void onPatternDraw(Canvas canvas) {
        if (pattern_matches_.isEmpty()) {
            return;
        }
        Iterator iterator = pattern_matches_.iterator();
        Integer id = (Integer)iterator.next();
        PatternKey key = pattern_keys_.get(id);
        PointF prev_pos = new PointF(key.getBounds().centerX(), key.getBounds().centerY());
        PointF cur_pos;

        while (iterator.hasNext()) {
            id = (Integer)iterator.next();
            key = pattern_keys_.get(id);
            cur_pos = new PointF(key.getBounds().centerX(), key.getBounds().centerY());
            canvas.drawLine(prev_pos.x, prev_pos.y, cur_pos.x, cur_pos.y, line_paint_);
            prev_pos = cur_pos;
        }

        if (!unlock_checking_) {
            return;
        }

        cur_pos = last_pos_;
        canvas.drawLine(prev_pos.x, prev_pos.y, cur_pos.x, cur_pos.y, line_paint_);
    }

}
