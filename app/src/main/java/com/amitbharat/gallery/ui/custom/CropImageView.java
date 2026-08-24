package com.amitbharat.gallery.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class CropImageView extends AppCompatImageView {

    public enum AspectRatio {
        FREE, SQUARE_1_1, RATIO_4_3, RATIO_16_9
    }

    private AspectRatio currentRatio = AspectRatio.FREE;
    private final RectF cropRect = new RectF();
    private final RectF imageBounds = new RectF();

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int rotationAngle = 0;
    private boolean isFlippedH = false;
    private boolean isFlippedV = false;

    private float lastTouchX, lastTouchY;
    // 0: None, 1: Move, 2: TL, 3: TR, 4: BL, 5: BR, 6: Top, 7: Bottom, 8: Left, 9: Right
    private int dragHandle = 0;
    private static final float HANDLE_TOUCH_RADIUS = 90f;
    private static final float MIN_CROP_SIZE = 80f;

    public CropImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_CENTER);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        gridPaint.setColor(Color.argb(140, 255, 255, 255));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f);

        shadowPaint.setColor(Color.argb(175, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(7f);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    private android.graphics.ColorMatrixColorFilter activeColorFilter;

    public void setColorMatrix(ColorMatrix matrix) {
        if (matrix != null) {
            activeColorFilter = new android.graphics.ColorMatrixColorFilter(matrix);
            setColorFilter(activeColorFilter);
            if (getDrawable() != null) {
                getDrawable().mutate().setColorFilter(activeColorFilter);
            }
        } else {
            activeColorFilter = null;
            clearColorFilter();
            if (getDrawable() != null) {
                getDrawable().mutate().clearColorFilter();
            }
        }
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (activeColorFilter != null && getDrawable() != null) {
            getDrawable().mutate().setColorFilter(activeColorFilter);
        }
        post(() -> {
            updateImageBounds();
            resetCropRect();
            invalidate();
        });
    }

    public void setAspectRatio(AspectRatio ratio) {
        this.currentRatio = ratio;
        resetCropRect();
        invalidate();
    }

    public void rotate90() {
        rotationAngle = (rotationAngle + 90) % 360;
        setRotation(rotationAngle);
        post(() -> {
            updateImageBounds();
            resetCropRect();
            invalidate();
        });
    }

    public void flipHorizontal() {
        isFlippedH = !isFlippedH;
        setScaleX(isFlippedH ? -1 : 1);
    }

    public void flipVertical() {
        isFlippedV = !isFlippedV;
        setScaleY(isFlippedV ? -1 : 1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateImageBounds();
        if (changed || cropRect.isEmpty()) {
            resetCropRect();
        }
    }

    private void updateImageBounds() {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() <= 0 || getHeight() <= 0) {
            imageBounds.set(0, 0, getWidth(), getHeight());
            return;
        }

        Matrix matrix = getImageMatrix();
        RectF drawableRect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(imageBounds, drawableRect);

        if (imageBounds.isEmpty() || imageBounds.width() <= 0) {
            imageBounds.set(0, 0, getWidth(), getHeight());
        }
    }

    private void resetCropRect() {
        updateImageBounds();
        if (imageBounds.isEmpty() || imageBounds.width() <= 0 || imageBounds.height() <= 0) return;

        float padX = imageBounds.width() * 0.05f;
        float padY = imageBounds.height() * 0.05f;
        float availW = imageBounds.width() - (2 * padX);
        float availH = imageBounds.height() - (2 * padY);

        float cropW = availW;
        float cropH = availH;

        if (currentRatio == AspectRatio.SQUARE_1_1) {
            float min = Math.min(availW, availH);
            cropW = min;
            cropH = min;
        } else if (currentRatio == AspectRatio.RATIO_4_3) {
            if (availW / availH > 4f / 3f) {
                cropH = availH;
                cropW = cropH * (4f / 3f);
            } else {
                cropW = availW;
                cropH = cropW * (3f / 4f);
            }
        } else if (currentRatio == AspectRatio.RATIO_16_9) {
            if (availW / availH > 16f / 9f) {
                cropH = availH;
                cropW = cropH * (16f / 9f);
            } else {
                cropW = availW;
                cropH = cropW * (9f / 16f);
            }
        }

        float left = imageBounds.left + (imageBounds.width() - cropW) / 2f;
        float top = imageBounds.top + (imageBounds.height() - cropH) / 2f;
        cropRect.set(left, top, left + cropW, top + cropH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || cropRect.isEmpty()) return;

        // Dim outside crop bounds
        canvas.drawRect(0, 0, w, cropRect.top, shadowPaint);
        canvas.drawRect(0, cropRect.bottom, w, h, shadowPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, shadowPaint);
        canvas.drawRect(cropRect.right, cropRect.top, w, cropRect.bottom, shadowPaint);

        // Crop outline rectangle
        canvas.drawRect(cropRect, borderPaint);

        // 3x3 Rule-of-Thirds Grid
        float colWidth = cropRect.width() / 3f;
        float rowHeight = cropRect.height() / 3f;
        canvas.drawLine(cropRect.left + colWidth, cropRect.top, cropRect.left + colWidth, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left + 2 * colWidth, cropRect.top, cropRect.left + 2 * colWidth, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + rowHeight, cropRect.right, cropRect.top + rowHeight, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + 2 * rowHeight, cropRect.right, cropRect.top + 2 * rowHeight, gridPaint);

        // Corner Guides
        float cornerLen = Math.min(42f, Math.min(cropRect.width(), cropRect.height()) / 4f);
        // Top-Left
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left + cornerLen, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left, cropRect.top + cornerLen, cornerPaint);
        // Top-Right
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right - cornerLen, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right, cropRect.top + cornerLen, cornerPaint);
        // Bottom-Left
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.left + cornerLen, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.left, cropRect.bottom - cornerLen, cornerPaint);
        // Bottom-Right
        canvas.drawLine(cropRect.right, cropRect.bottom, cropRect.right - cornerLen, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.bottom, cropRect.right, cropRect.bottom - cornerLen, cornerPaint);

        // Edge Grip Handles
        float edgeHandleLen = 30f;
        // Top edge
        canvas.drawLine(cropRect.centerX() - edgeHandleLen, cropRect.top, cropRect.centerX() + edgeHandleLen, cropRect.top, cornerPaint);
        // Bottom edge
        canvas.drawLine(cropRect.centerX() - edgeHandleLen, cropRect.bottom, cropRect.centerX() + edgeHandleLen, cropRect.bottom, cornerPaint);
        // Left edge
        canvas.drawLine(cropRect.left, cropRect.centerY() - edgeHandleLen, cropRect.left, cropRect.centerY() + edgeHandleLen, cornerPaint);
        // Right edge
        canvas.drawLine(cropRect.right, cropRect.centerY() - edgeHandleLen, cropRect.right, cropRect.centerY() + edgeHandleLen, cornerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;
                dragHandle = getTouchHandle(x, y);
                return dragHandle != 0;

            case MotionEvent.ACTION_MOVE:
                if (dragHandle == 0) return false;
                float dx = x - lastTouchX;
                float dy = y - lastTouchY;

                updateImageBounds();
                float boundL = imageBounds.left;
                float boundT = imageBounds.top;
                float boundR = imageBounds.right;
                float boundB = imageBounds.bottom;

                if (dragHandle == 1) { // Pan / Move Entire Crop Box
                    float newL = cropRect.left + dx;
                    float newR = cropRect.right + dx;
                    float newT = cropRect.top + dy;
                    float newB = cropRect.bottom + dy;

                    if (newL >= boundL && newR <= boundR) {
                        cropRect.left = newL;
                        cropRect.right = newR;
                    } else if (newL < boundL) {
                        float w = cropRect.width();
                        cropRect.left = boundL;
                        cropRect.right = boundL + w;
                    } else if (newR > boundR) {
                        float w = cropRect.width();
                        cropRect.right = boundR;
                        cropRect.left = boundR - w;
                    }

                    if (newT >= boundT && newB <= boundB) {
                        cropRect.top = newT;
                        cropRect.bottom = newB;
                    } else if (newT < boundT) {
                        float h = cropRect.height();
                        cropRect.top = boundT;
                        cropRect.bottom = boundT + h;
                    } else if (newB > boundB) {
                        float h = cropRect.height();
                        cropRect.bottom = boundB;
                        cropRect.top = boundB - h;
                    }
                } else if (currentRatio == AspectRatio.FREE) {
                    // Custom Free Crop Handles (All 4 Corners + All 4 Edges)
                    switch (dragHandle) {
                        case 2: // Top-Left
                            cropRect.left = Math.min(cropRect.right - MIN_CROP_SIZE, Math.max(boundL, cropRect.left + dx));
                            cropRect.top = Math.min(cropRect.bottom - MIN_CROP_SIZE, Math.max(boundT, cropRect.top + dy));
                            break;
                        case 3: // Top-Right
                            cropRect.right = Math.max(cropRect.left + MIN_CROP_SIZE, Math.min(boundR, cropRect.right + dx));
                            cropRect.top = Math.min(cropRect.bottom - MIN_CROP_SIZE, Math.max(boundT, cropRect.top + dy));
                            break;
                        case 4: // Bottom-Left
                            cropRect.left = Math.min(cropRect.right - MIN_CROP_SIZE, Math.max(boundL, cropRect.left + dx));
                            cropRect.bottom = Math.max(cropRect.top + MIN_CROP_SIZE, Math.min(boundB, cropRect.bottom + dy));
                            break;
                        case 5: // Bottom-Right
                            cropRect.right = Math.max(cropRect.left + MIN_CROP_SIZE, Math.min(boundR, cropRect.right + dx));
                            cropRect.bottom = Math.max(cropRect.top + MIN_CROP_SIZE, Math.min(boundB, cropRect.bottom + dy));
                            break;
                        case 6: // Top Edge
                            cropRect.top = Math.min(cropRect.bottom - MIN_CROP_SIZE, Math.max(boundT, cropRect.top + dy));
                            break;
                        case 7: // Bottom Edge
                            cropRect.bottom = Math.max(cropRect.top + MIN_CROP_SIZE, Math.min(boundB, cropRect.bottom + dy));
                            break;
                        case 8: // Left Edge
                            cropRect.left = Math.min(cropRect.right - MIN_CROP_SIZE, Math.max(boundL, cropRect.left + dx));
                            break;
                        case 9: // Right Edge
                            cropRect.right = Math.max(cropRect.left + MIN_CROP_SIZE, Math.min(boundR, cropRect.right + dx));
                            break;
                    }
                } else {
                    // Ratio Locked Resize
                    float targetRatio = 1f;
                    if (currentRatio == AspectRatio.RATIO_4_3) targetRatio = 4f / 3f;
                    else if (currentRatio == AspectRatio.RATIO_16_9) targetRatio = 16f / 9f;

                    if (dragHandle == 5 || dragHandle == 9 || dragHandle == 7) { // BR / Right / Bottom
                        float newW = Math.max(MIN_CROP_SIZE, Math.min(boundR - cropRect.left, cropRect.width() + dx));
                        float newH = newW / targetRatio;
                        if (cropRect.top + newH <= boundB) {
                            cropRect.right = cropRect.left + newW;
                            cropRect.bottom = cropRect.top + newH;
                        }
                    } else if (dragHandle == 2 || dragHandle == 8 || dragHandle == 6) { // TL / Left / Top
                        float newW = Math.max(MIN_CROP_SIZE, Math.min(cropRect.right - boundL, cropRect.width() - dx));
                        float newH = newW / targetRatio;
                        if (cropRect.bottom - newH >= boundT) {
                            cropRect.left = cropRect.right - newW;
                            cropRect.top = cropRect.bottom - newH;
                        }
                    } else if (dragHandle == 3) { // TR
                        float newW = Math.max(MIN_CROP_SIZE, Math.min(boundR - cropRect.left, cropRect.width() + dx));
                        float newH = newW / targetRatio;
                        if (cropRect.bottom - newH >= boundT) {
                            cropRect.right = cropRect.left + newW;
                            cropRect.top = cropRect.bottom - newH;
                        }
                    } else if (dragHandle == 4) { // BL
                        float newW = Math.max(MIN_CROP_SIZE, Math.min(cropRect.right - boundL, cropRect.width() - dx));
                        float newH = newW / targetRatio;
                        if (cropRect.top + newH <= boundB) {
                            cropRect.left = cropRect.right - newW;
                            cropRect.bottom = cropRect.top + newH;
                        }
                    }
                }

                lastTouchX = x;
                lastTouchY = y;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragHandle = 0;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int getTouchHandle(float x, float y) {
        // Corners have highest priority
        if (Math.hypot(x - cropRect.left, y - cropRect.top) < HANDLE_TOUCH_RADIUS) return 2; // TL
        if (Math.hypot(x - cropRect.right, y - cropRect.top) < HANDLE_TOUCH_RADIUS) return 3; // TR
        if (Math.hypot(x - cropRect.left, y - cropRect.bottom) < HANDLE_TOUCH_RADIUS) return 4; // BL
        if (Math.hypot(x - cropRect.right, y - cropRect.bottom) < HANDLE_TOUCH_RADIUS) return 5; // BR

        // Edges
        if (Math.abs(y - cropRect.top) < HANDLE_TOUCH_RADIUS && x >= cropRect.left && x <= cropRect.right) return 6; // Top
        if (Math.abs(y - cropRect.bottom) < HANDLE_TOUCH_RADIUS && x >= cropRect.left && x <= cropRect.right) return 7; // Bottom
        if (Math.abs(x - cropRect.left) < HANDLE_TOUCH_RADIUS && y >= cropRect.top && y <= cropRect.bottom) return 8; // Left
        if (Math.abs(x - cropRect.right) < HANDLE_TOUCH_RADIUS && y >= cropRect.top && y <= cropRect.bottom) return 9; // Right

        // Center / Inside Move
        if (cropRect.contains(x, y)) return 1;

        return 0;
    }

    public Bitmap cropBitmap(Bitmap src) {
        if (src == null || cropRect.isEmpty()) return src;

        try {
            Matrix matrix = new Matrix();
            if (rotationAngle != 0) {
                matrix.postRotate(rotationAngle);
            }
            if (isFlippedH) {
                matrix.postScale(-1, 1);
            }
            if (isFlippedV) {
                matrix.postScale(1, -1);
            }

            Bitmap transformed = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);

            updateImageBounds();
            if (imageBounds.width() <= 0 || imageBounds.height() <= 0) return transformed;

            float scaleX = (float) transformed.getWidth() / imageBounds.width();
            float scaleY = (float) transformed.getHeight() / imageBounds.height();

            int cropX = (int) Math.max(0, (cropRect.left - imageBounds.left) * scaleX);
            int cropY = (int) Math.max(0, (cropRect.top - imageBounds.top) * scaleY);
            int cropW = (int) Math.min(transformed.getWidth() - cropX, cropRect.width() * scaleX);
            int cropH = (int) Math.min(transformed.getHeight() - cropY, cropRect.height() * scaleY);

            if (cropW > 0 && cropH > 0) {
                return Bitmap.createBitmap(transformed, cropX, cropY, cropW, cropH);
            }
            return transformed;
        } catch (Exception e) {
            e.printStackTrace();
            return src;
        }
    }
}
