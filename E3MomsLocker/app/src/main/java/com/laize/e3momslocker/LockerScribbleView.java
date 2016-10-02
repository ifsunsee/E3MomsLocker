// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

public class LockerScribbleView extends View {
    final private int DEFAULT_MAX_LINES = 30;
    final private int DEFAULT_MAX_POINTS = 1000;
    final private float DEFAULT_LINE_WIDTH = 20.0f;

    private boolean making_line_;

    private ArrayList<Path> points_list_;
    private ArrayList<Paint> line_paint_list_;

    private int current_color_;
    private ArrayList<PointF> current_points_;
    private Paint current_paint_;

    private int max_lines_;
    private int max_points_;

    private float line_width_;

    public LockerScribbleView(Context context) {
        super(context);
        init(null, 0);
    }

    public LockerScribbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LockerScribbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.setBackgroundColor(Color.TRANSPARENT);

        current_color_ = Color.RED;
        points_list_ = new ArrayList<Path>();
        line_paint_list_ = new ArrayList<Paint>();

        max_lines_ = DEFAULT_MAX_LINES;
        max_points_ = DEFAULT_MAX_POINTS;
        line_width_ = DEFAULT_LINE_WIDTH;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            making_line_ = true;
            current_paint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
            current_paint_.setColor(current_color_);
            current_paint_.setStyle(Paint.Style.STROKE);
            current_paint_.setStrokeCap(Paint.Cap.ROUND);
            current_paint_.setStrokeJoin(Paint.Join.ROUND);
            current_paint_.setStrokeWidth(line_width_);
            CornerPathEffect pathEffect = new CornerPathEffect(line_width_);
            current_paint_.setPathEffect(pathEffect);

            current_points_ = new ArrayList<PointF>();
            current_points_.add(new PointF(event.getX(), event.getY()));
            this.invalidate();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            making_line_ = false;
            if (current_points_.size() > 1) {
                updateDrawList(current_points_, current_paint_);
                current_points_ = null;
                current_paint_ = null;
            }
            this.invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE && making_line_) {
            current_points_.add(new PointF(event.getX(), event.getY()));
            if (current_points_.size() > max_points_)
                current_points_.remove(0);
            this.invalidate();
            return true;
        }

        return super.dispatchTouchEvent(event);
    }

    private void updateDrawList(ArrayList<PointF> points, Paint line_paint) {
        Path path = new Path();

        Iterator iterator = points.iterator();
        float pre_x = 0, pre_y = 0;
        if (iterator.hasNext()) {
            PointF point = (PointF)iterator.next();
            path.moveTo(point.x, point.y);
        }

        while (iterator.hasNext()) {
            PointF point = (PointF)iterator.next();
            path.lineTo(point.x, point.y);
        }

        points_list_.add(path);
        line_paint_list_.add(line_paint);

        if (points_list_.size() > max_lines_) {
            points_list_.remove(0);
            line_paint_list_.remove(0);
        }

        float alpha_weight = 255.0f / max_lines_ ;
        float current_alpha = alpha_weight * (max_lines_ - line_paint_list_.size());
        for (Object object : line_paint_list_) {
            Paint paint = (Paint)object;
            current_alpha = current_alpha + alpha_weight;
            paint.setAlpha((int) current_alpha);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Iterator iterator_points_list = points_list_.iterator();
        Iterator iterator_line_paint_list = line_paint_list_.iterator();
        while (iterator_points_list.hasNext()) {
            Path path = (Path)iterator_points_list.next();
            Paint paint = (Paint)iterator_line_paint_list.next();
            canvas.drawPath(path, paint);
        }

        if (current_points_ != null) {
            onDrawPoints(canvas, current_points_, current_paint_);
        }
    }

    private void onDrawPoints(Canvas canvas, ArrayList<PointF> points, Paint paint) {
        Path path = new Path();

        Iterator iterator = points.iterator();
        if (iterator.hasNext()) {
            PointF point = (PointF)iterator.next();
            path.moveTo(point.x, point.y);
        }

        while (iterator.hasNext()) {
            PointF point = (PointF)iterator.next();
            path.lineTo(point.x, point.y);
        }

        canvas.drawPath(path, paint);
    }

    public void setColor(int color) {
        // remove alpha value.
        current_color_ = color | Color.BLACK;
    }

    public void clearAllLines() {
        points_list_.clear();
        line_paint_list_.clear();

        this.invalidate();
    }
}
