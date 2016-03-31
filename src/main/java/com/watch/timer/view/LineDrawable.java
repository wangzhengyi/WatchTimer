package com.watch.timer.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;

@SuppressWarnings("unused")
public class LineDrawable extends Drawable {
    private LineShape mShape;
    private Paint mPaint;

    public LineDrawable(PointF inPoint, PointF extPoint) {
        this.mShape = new LineShape(inPoint.x, inPoint.y, extPoint.x, extPoint.y);

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        mPaint.setStrokeWidth(width);
    }


    @Override
    public void draw(Canvas canvas) {
        if (mShape != null) {
            mShape.draw(canvas, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private class LineShape extends Shape {
        private Path path;

        public LineShape(float left, float top, float right, float bottom) {
            this.path = new Path();
            this.path.setFillType(Path.FillType.WINDING);
            this.path.moveTo(left, top);
            this.path.lineTo(right, bottom);
        }


        @Override
        public void draw(Canvas canvas, Paint paint) {
            if (path != null) {
                canvas.drawPath(path, paint);
            }
        }
    }
}
