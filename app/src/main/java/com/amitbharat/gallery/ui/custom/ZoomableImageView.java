package com.amitbharat.gallery.ui.custom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView implements
        ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private Matrix matrix;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final PointF last = new PointF();
    private final PointF start = new PointF();
    private float minScale = 1f;
    private float maxScale = 5f;
    private float currentScale = 1f;

    private int viewWidth, viewHeight;
    private static final int CLICK = 6;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private OnClickListener singleClickListener;

    public ZoomableImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        super.setClickable(true);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);
        matrix = new Matrix();
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
    }

    public void setOnSingleClickListener(OnClickListener listener) {
        this.singleClickListener = listener;
    }

    public boolean isZoomed() {
        return currentScale > 1.05f;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::fitImageToView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        fitImageToView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                last.set(curr);
                start.set(last);
                mode = DRAG;
                if (isZoomed() && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG && !mScaleDetector.isInProgress()) {
                    float deltaX = curr.x - last.x;
                    float deltaY = curr.y - last.y;

                    if (isZoomed()) {
                        if (getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            if (rect.width() <= viewWidth) {
                                deltaX = 0;
                            }
                            if (rect.height() <= viewHeight) {
                                deltaY = 0;
                            }
                            matrix.postTranslate(deltaX, deltaY);
                            fixTrans();
                            setImageMatrix(matrix);
                            invalidate();
                        }
                    }
                    last.set(curr.x, curr.y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mode = NONE;
                int xDiff = (int) Math.abs(curr.x - start.x);
                int yDiff = (int) Math.abs(curr.y - start.y);
                if (xDiff < CLICK && yDiff < CLICK) {
                    performClick();
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float mScaleFactor = detector.getScaleFactor();
        float origScale = currentScale;
        currentScale *= mScaleFactor;

        if (currentScale > maxScale) {
            currentScale = maxScale;
            mScaleFactor = maxScale / origScale;
        } else if (currentScale < minScale) {
            currentScale = minScale;
            mScaleFactor = minScale / origScale;
        }

        matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
        fixTrans();
        setImageMatrix(matrix);
        invalidate();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mode = ZOOM;
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mode = NONE;
        if (currentScale <= 1.05f) {
            fitImageToView();
        }
    }

    private RectF getDisplayRect() {
        Drawable drawable = getDrawable();
        if (drawable == null) return null;
        RectF rect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);
        return rect;
    }

    private void fixTrans() {
        RectF rect = getDisplayRect();
        if (rect == null || viewWidth == 0 || viewHeight == 0) return;

        float deltaX = 0, deltaY = 0;

        if (rect.width() <= viewWidth) {
            deltaX = (viewWidth - rect.width()) / 2f - rect.left;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
        }

        if (rect.height() <= viewHeight) {
            deltaY = (viewHeight - rect.height()) / 2f - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        if (deltaX != 0 || deltaY != 0) {
            matrix.postTranslate(deltaX, deltaY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (currentScale == 1f) {
            fitImageToView();
        }
    }

    public void fitImageToView() {
        if (viewWidth == 0 || viewHeight == 0) return;
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) return;

        int bmWidth = drawable.getIntrinsicWidth();
        int bmHeight = drawable.getIntrinsicHeight();

        float scaleX = (float) viewWidth / (float) bmWidth;
        float scaleY = (float) viewHeight / (float) bmHeight;
        float scale = Math.min(scaleX, scaleY);

        matrix.reset();
        matrix.setScale(scale, scale);

        float redundantXSpace = (viewWidth - (scale * bmWidth)) / 2f;
        float redundantYSpace = (viewHeight - (scale * bmHeight)) / 2f;
        matrix.postTranslate(redundantXSpace, redundantYSpace);

        setImageMatrix(matrix);
        currentScale = 1f;
        invalidate();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (singleClickListener != null) {
            singleClickListener.onClick(this);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (isZoomed()) {
            fitImageToView();
        } else {
            float targetScale = 2.5f;
            float factor = targetScale / currentScale;
            currentScale = targetScale;
            matrix.postScale(factor, factor, e.getX(), e.getY());
            fixTrans();
            setImageMatrix(matrix);
            invalidate();
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) { return false; }
    @Override
    public boolean onDown(MotionEvent e) { return true; }
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) { return false; }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override
    public void onLongPress(MotionEvent e) {}
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
}
