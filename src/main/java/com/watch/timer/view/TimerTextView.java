package com.watch.timer.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.watch.timer.R;

import java.util.List;

@SuppressWarnings("unused")
public class TimerTextView extends View {
    /**
     * 默认中心文字大小(px)
     */
    private static final float DEFAULT_CENTER_TEXT_SIZE = 60;

    /**
     * 默认两侧文字大小(px)
     */
    private static final float DEFAULT_SIDE_TEXT_SIZE = 22;

    /**
     * 默认中心文字的颜色
     */
    private static final int DEFAULT_CENTER_TEXT_COLOR = Color.parseColor("#ffffff");

    /**
     * 默认两侧文字的颜色
     */
    private static final int DEFAULT_SIDE_TEXT_COLOR = Color.parseColor("#808080");

    /**
     * 默认中心文字距离上方文字的间距
     */
    private static final float DEFAULT_CENTER_MARGIN_TOP = 16;

    /**
     * 默认中心文字距离下方文字的间距
     */
    private static final float DEFAULT_CENTER_MARGIN_BOTTOM = 18;

    /**
     * 中心文字的画笔
     */
    private Paint mCenterPaint;

    /**
     * 中心文字的颜色
     */
    private int mCenterTextColor;

    /**
     * 中心文字大小
     */
    private float mCenterTextSize;

    /**
     * 两侧文字的画笔
     */
    private Paint mSidePaint;

    /**
     * 两侧文字的颜色
     */
    private int mSideTextColor;

    /**
     * 两侧文字的大小
     */
    private float mSideTextSize;

    /**
     * 中心文字距离上方文字的间距
     */
    private float mCenterMarginTop;

    /**
     * 中心文字距离下方文字的间距
     */
    private float mCenterMarginBottom;

    /**
     * View中心点的X和Y坐标
     */
    private float mCenterX, mCenterY;

    /**
     * 中心文字内容
     */
    private List<String> mItemList;

    /**
     * 刻度内容(小时或者分钟)
     */
    private String mScaleTextContent;

    /**
     * 中心文字所在List的position
     */
    private int mSelectedPosition;

    /**
     * 记录Touch移动的距离
     */
    private float mMoveY;

    /**
     * 记录手指按下的距离
     */
    private float mLastDownY;

    /**
     * Touch事件需要改变的高度.
     */
    private int mTouchChangeHeight;

    /**
     * 文字的Align
     */
    private Paint.Align mPaintAlign;

    /**
     * 是否呈现Bottom文本
     */
    private boolean mIsShowBottomText;

    public TimerTextView(Context context) {
        this(context, null);
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimerTextView);
        mCenterTextColor = ta.getColor(
                R.styleable.TimerTextView_center_text_color, DEFAULT_CENTER_TEXT_COLOR);
        mCenterTextSize = ta.getDimension(R.styleable.TimerTextView_center_text_size,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_CENTER_TEXT_SIZE,
                        getResources().getDisplayMetrics()));
        mSideTextColor = ta.getColor(
                R.styleable.TimerTextView_side_text_color, DEFAULT_SIDE_TEXT_COLOR);
        mSideTextSize = ta.getDimension(R.styleable.TimerTextView_side_text_size,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_SIDE_TEXT_SIZE,
                        getResources().getDisplayMetrics()));
        mCenterMarginTop = ta.getDimension(R.styleable.TimerTextView_center_margin_top,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_CENTER_MARGIN_TOP,
                        getResources().getDisplayMetrics()));
        mCenterMarginBottom = ta.getDimension(R.styleable.TimerTextView_center_margin_bottom,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_CENTER_MARGIN_BOTTOM,
                        getResources().getDisplayMetrics()));
        mIsShowBottomText = ta.getBoolean(R.styleable.TimerTextView_show_bottom_text, false);
        int paintAlign = ta.getInt(R.styleable.TimerTextView_paint_align, 1);
        initPaintAlign(paintAlign);
        ta.recycle();

        initPaint();
    }

    private void initPaintAlign(int paintAlign) {
        switch (paintAlign) {
            case 0:
                mPaintAlign = Paint.Align.LEFT;
                break;
            case 1:
                mPaintAlign = Paint.Align.CENTER;
                break;
            case 2:
                mPaintAlign = Paint.Align.RIGHT;
                break;
            default:
                mPaintAlign = Paint.Align.CENTER;
                break;
        }
    }

    private void initPaint() {
        mCenterPaint = createPaint(mCenterTextSize, mCenterTextColor);
        mSidePaint = createPaint(mSideTextSize, mSideTextColor);
        initTouchChangeHeight();
    }

    private void initTouchChangeHeight() {
        Paint.FontMetricsInt fmi = mCenterPaint.getFontMetricsInt();
        mTouchChangeHeight = fmi.ascent * -1;
    }

    private Paint createPaint(float textSize, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(color);
        paint.setTextAlign(mPaintAlign);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();

        mCenterX = (float) (viewWidth / 2.0);
        mCenterY = (float) (viewHeight / 2.0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mItemList != null && !mItemList.isEmpty()) {
            drawCenterText(canvas);
            drawScaleText(canvas);
            if (mIsShowBottomText) {
                drawSideText(canvas);
            }
        }
    }

    private void drawSideText(Canvas canvas) {
        int nextItem = (mSelectedPosition + 1) % mItemList.size();
        float baseLineY = calculateBaseLineForBottom();
        canvas.drawText(mItemList.get(nextItem), mCenterX, baseLineY, mSidePaint);
    }

    private void drawScaleText(Canvas canvas) {
        if (!TextUtils.isEmpty(mScaleTextContent)) {
            float baseLineY = calculateBaseLineForTop();
            canvas.drawText(mScaleTextContent, mCenterX, baseLineY, mSidePaint);
        }
    }

    private void drawCenterText(Canvas canvas) {
        float baseLineY = calculateBaseLineForCenter();
        canvas.drawText(mItemList.get(mSelectedPosition), mCenterX, baseLineY, mCenterPaint);
    }

    private float calculateBaseLineForCenter() {
        // ascent = ascent线的y坐标 - baseline线的y坐标
        // descent = descent线的y坐标 - baseline线的y坐标
        // top = top线的y坐标 - baseline线的y坐标
        // bottom = bottom线的y坐标 - baseline线的y坐标
        Paint.FontMetricsInt fmi = mCenterPaint.getFontMetricsInt();
        float centerY = mCenterY + mMoveY;
        return (fmi.descent - fmi.ascent) / 2.0F - fmi.descent + centerY;
    }

    private float calculateBaseLineForBottom() {
        // 获取中心文字的下边界
        Paint.FontMetricsInt fmi = mCenterPaint.getFontMetricsInt();
        int centerTextHeight = fmi.bottom - fmi.top;
        float centerY = mCenterY + mMoveY;
        float centerTextBottomY = centerY + centerTextHeight / 2.0F;

        // 获取下方文字baseline距离top的大小
        Paint.FontMetricsInt bFmi = mSidePaint.getFontMetricsInt();
        return (centerTextBottomY + mCenterMarginBottom - bFmi.top);
    }

    private float calculateBaseLineForTop() {
        // 获取中心文字的上边界
        Paint.FontMetricsInt fmi = mCenterPaint.getFontMetricsInt();
        int centerTextHeight = fmi.bottom - fmi.top;
        float centerY = mCenterY;
        float centerTextTopY = centerY - centerTextHeight / 2.0F;

        // 获取上方文字baseline距离bottom的大小
        Paint.FontMetricsInt tFmi = mSidePaint.getFontMetricsInt();
        return (centerTextTopY - mCenterMarginTop - tFmi.bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveY += event.getY() - mLastDownY;
                if (mMoveY > mTouchChangeHeight / 2.0F) {
                    mMoveY = 0;
                    mSelectedPosition--;
                    if (mSelectedPosition < 0) {
                        mSelectedPosition = mItemList.size() - 1;
                    }
                } else if (mMoveY * -1 > mTouchChangeHeight / 2.0F) {
                    mMoveY = 0;
                    mSelectedPosition = (++mSelectedPosition) % mItemList.size();
                }
                mLastDownY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mMoveY = 0;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public void setItemListAndScaleContent(List<String> list, String content) {
        mItemList = list;
        mScaleTextContent = content;
        if (mItemList != null) {
            resetCurrentSelect();
        }
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        resetCurrentSelect();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    private void resetCurrentSelect() {
        if (mSelectedPosition < 0) {
            mSelectedPosition = 0;
        }

        if (mSelectedPosition > mItemList.size()) {
            mSelectedPosition = mItemList.size() - 1;
        }

        invalidate();
    }
}
