package com.amitbharat.gallery.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.amitbharat.gallery.utils.ImageEnhancer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollageView extends View {

    public enum AspectRatio {
        RATIO_1_1(1.0f, "1:1"),
        RATIO_4_5(4.0f / 5.0f, "4:5"),
        RATIO_3_4(3.0f / 4.0f, "3:4"),
        RATIO_16_9(16.0f / 9.0f, "16:9"),
        RATIO_9_16(9.0f / 16.0f, "9:16");

        private final float ratio;
        private final String label;

        AspectRatio(float ratio, String label) {
            this.ratio = ratio;
            this.label = label;
        }

        public float getRatio() {
            return ratio;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum BackgroundType {
        SOLID,
        GRADIENT
    }

    public static class GradientPreset {
        public final String name;
        public final int[] colors;
        public final float[] positions;

        public GradientPreset(String name, int[] colors) {
            this.name = name;
            this.colors = colors;
            this.positions = null;
        }
    }

    public static class Template {
        public final String name;
        public final RectF[] bounds;

        public Template(String name, RectF[] bounds) {
            this.name = name;
            this.bounds = bounds;
        }
    }

    public static class TileState {
        public Bitmap bitmap;
        public int rotationDegrees = 0;
        public boolean flipH = false;
        public boolean flipV = false;
        public ImageEnhancer.FilterType filter = ImageEnhancer.FilterType.NONE;
        public float zoom = 1.0f;

        public TileState(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public static class TextOverlay {
        public String text;
        public float x = 0.5f; // normalized center 0..1
        public float y = 0.5f;
        public int textColor = Color.WHITE;
        public int bgColor = 0xAA000000;
        public float textSizeDp = 22f;

        public TextOverlay(String text, int textColor, int bgColor) {
            this.text = text;
            this.textColor = textColor;
            this.bgColor = bgColor;
        }
    }

    public static class StickerOverlay {
        public String emoji;
        public float x = 0.5f;
        public float y = 0.5f;
        public float sizeDp = 42f;

        public StickerOverlay(String emoji, float x, float y) {
            this.emoji = emoji;
            this.x = x;
            this.y = y;
        }
    }

    public static class DoodleStroke {
        public Path path = new Path();
        public int color = Color.RED;
        public float strokeWidthDp = 4f;

        public DoodleStroke(int color, float strokeWidthDp) {
            this.color = color;
            this.strokeWidthDp = strokeWidthDp;
        }
    }

    public enum InteractiveMode {
        SELECT_TILE,
        DOODLE,
        DRAG_OVERLAY
    }

    private final List<TileState> tiles = new ArrayList<>();
    private final List<TextOverlay> textOverlays = new ArrayList<>();
    private final List<StickerOverlay> stickerOverlays = new ArrayList<>();
    private final List<DoodleStroke> doodleStrokes = new ArrayList<>();

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stickerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint doodlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();

    private AspectRatio currentRatio = AspectRatio.RATIO_1_1;
    private int currentTemplateIndex = 0;
    private float innerPaddingDp = 6f;
    private float outerMarginDp = 8f;
    private float cornerRadiusDp = 10f;
    private boolean enableTileShadow = true;

    private BackgroundType backgroundType = BackgroundType.SOLID;
    private int solidBgColor = Color.WHITE;
    private GradientPreset currentGradient = null;

    private int selectedTileIndex = -1;
    private final List<RectF> computedTileRects = new ArrayList<>();

    private InteractiveMode interactiveMode = InteractiveMode.SELECT_TILE;
    private DoodleStroke currentDoodleStroke = null;
    private int doodleColor = 0xFFFF1744;
    private float doodleStrokeWidthDp = 5f;

    private Object draggedOverlay = null;
    private PointF dragStartTouch = new PointF();

    public interface OnCollageEventListener {
        void onTileSelected(int index, TileState tile);
        void onTileDeselected();
        void onTilesSwapped(int index1, int index2);
    }

    private OnCollageEventListener eventListener;

    public CollageView(Context context) {
        super(context);
        init();
    }

    public CollageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CollageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setColor(solidBgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        highlightPaint.setColor(0xFF448AFF);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(dpToPx(3.5f));

        shadowPaint.setColor(0x33000000);
        shadowPaint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        textBgPaint.setStyle(Paint.Style.FILL);

        stickerPaint.setTextAlign(Paint.Align.CENTER);

        doodlePaint.setStyle(Paint.Style.STROKE);
        doodlePaint.setStrokeCap(Paint.Cap.ROUND);
        doodlePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setOnCollageEventListener(OnCollageEventListener listener) {
        this.eventListener = listener;
    }

    public void setBitmaps(List<Bitmap> bitmaps) {
        this.tiles.clear();
        if (bitmaps != null) {
            for (Bitmap b : bitmaps) {
                this.tiles.add(new TileState(b));
            }
        }
        this.selectedTileIndex = -1;
        this.currentTemplateIndex = 0;
        requestLayout();
        invalidate();
    }

    public List<TileState> getTiles() {
        return tiles;
    }

    public TileState getSelectedTile() {
        if (selectedTileIndex >= 0 && selectedTileIndex < tiles.size()) {
            return tiles.get(selectedTileIndex);
        }
        return null;
    }

    public int getSelectedTileIndex() {
        return selectedTileIndex;
    }

    public void deselectTile() {
        selectedTileIndex = -1;
        if (eventListener != null) {
            eventListener.onTileDeselected();
        }
        invalidate();
    }

    public void rotateSelectedTile() {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.rotationDegrees = (tile.rotationDegrees + 90) % 360;
            invalidate();
        }
    }

    public void flipSelectedTileH() {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.flipH = !tile.flipH;
            invalidate();
        }
    }

    public void flipSelectedTileV() {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.flipV = !tile.flipV;
            invalidate();
        }
    }

    public void setFilterForSelectedTile(ImageEnhancer.FilterType filter) {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.filter = filter;
            invalidate();
        }
    }

    public void zoomSelectedTile(float factor) {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.zoom = Math.max(1.0f, Math.min(3.0f, tile.zoom * factor));
            invalidate();
        }
    }

    public void resetSelectedTileZoom() {
        TileState tile = getSelectedTile();
        if (tile != null) {
            tile.zoom = 1.0f;
            invalidate();
        }
    }

    public void setAspectRatio(AspectRatio ratio) {
        this.currentRatio = ratio;
        requestLayout();
        invalidate();
    }

    public AspectRatio getAspectRatio() {
        return currentRatio;
    }

    public void setTemplateIndex(int index) {
        List<Template> templates = getAvailableTemplates(tiles.size());
        if (index >= 0 && index < templates.size()) {
            this.currentTemplateIndex = index;
            invalidate();
        }
    }

    public int getTemplateIndex() {
        return currentTemplateIndex;
    }

    public void setInnerPadding(float dp) {
        this.innerPaddingDp = dp;
        invalidate();
    }

    public void setOuterMargin(float dp) {
        this.outerMarginDp = dp;
        invalidate();
    }

    public void setCornerRadius(float dp) {
        this.cornerRadiusDp = dp;
        invalidate();
    }

    public void setEnableTileShadow(boolean enable) {
        this.enableTileShadow = enable;
        invalidate();
    }

    public void setSolidBackgroundColor(int color) {
        this.backgroundType = BackgroundType.SOLID;
        this.solidBgColor = color;
        this.currentGradient = null;
        invalidate();
    }

    public void setGradientBackground(GradientPreset preset) {
        this.backgroundType = BackgroundType.GRADIENT;
        this.currentGradient = preset;
        invalidate();
    }

    // Text Overlay Management
    public void addTextOverlay(TextOverlay overlay) {
        textOverlays.add(overlay);
        invalidate();
    }

    public void removeTextOverlay(TextOverlay overlay) {
        textOverlays.remove(overlay);
        invalidate();
    }

    // Sticker Overlay Management
    public void addStickerOverlay(StickerOverlay sticker) {
        stickerOverlays.add(sticker);
        invalidate();
    }

    // Doodle Tool
    public void setInteractiveMode(InteractiveMode mode) {
        this.interactiveMode = mode;
        if (mode != InteractiveMode.SELECT_TILE) {
            selectedTileIndex = -1;
            if (eventListener != null) eventListener.onTileDeselected();
        }
        invalidate();
    }

    public InteractiveMode getInteractiveMode() {
        return interactiveMode;
    }

    public void setDoodleColor(int color) {
        this.doodleColor = color;
    }

    public void setDoodleStrokeWidth(float widthDp) {
        this.doodleStrokeWidthDp = widthDp;
    }

    public void undoLastDoodle() {
        if (!doodleStrokes.isEmpty()) {
            doodleStrokes.remove(doodleStrokes.size() - 1);
            invalidate();
        }
    }

    public void clearDoodles() {
        doodleStrokes.clear();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float ratio = currentRatio.getRatio();

        int measuredWidth = widthSize;
        int measuredHeight = heightSize;

        if (widthSize > 0 && heightSize > 0) {
            float availableRatio = (float) widthSize / (float) heightSize;
            if (availableRatio > ratio) {
                measuredWidth = (int) (heightSize * ratio);
                measuredHeight = heightSize;
            } else {
                measuredWidth = widthSize;
                measuredHeight = (int) (widthSize / ratio);
            }
        } else {
            measuredWidth = 1080;
            measuredHeight = (int) (measuredWidth / ratio);
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF bounds = new RectF(0, 0, getWidth(), getHeight());
        drawCollage(canvas, bounds, false);
    }

    private void drawCollage(Canvas canvas, RectF canvasBounds, boolean isExporting) {
        float scaleFactor = canvasBounds.width() / (isExporting ? 2048f : (float) Math.max(1, getWidth()));
        if (!isExporting) scaleFactor = 1.0f;

        // 1. Draw Background
        if (backgroundType == BackgroundType.GRADIENT && currentGradient != null) {
            Shader shader = new LinearGradient(
                    canvasBounds.left, canvasBounds.top,
                    canvasBounds.right, canvasBounds.bottom,
                    currentGradient.colors, null, Shader.TileMode.CLAMP);
            bgPaint.setShader(shader);
            canvas.drawRect(canvasBounds, bgPaint);
            bgPaint.setShader(null);
        } else {
            bgPaint.setColor(solidBgColor);
            canvas.drawRect(canvasBounds, bgPaint);
        }

        int count = tiles.size();
        if (count == 0) return;

        List<Template> templates = getAvailableTemplates(count);
        int templateIdx = Math.min(currentTemplateIndex, templates.size() - 1);
        Template template = templates.get(Math.max(0, templateIdx));

        float outerMarginPx = dpToPx(outerMarginDp) * scaleFactor;
        float innerPaddingPx = dpToPx(innerPaddingDp) * scaleFactor;
        float cornerRadiusPx = dpToPx(cornerRadiusDp) * scaleFactor;

        RectF contentArea = new RectF(
                canvasBounds.left + outerMarginPx,
                canvasBounds.top + outerMarginPx,
                canvasBounds.right - outerMarginPx,
                canvasBounds.bottom - outerMarginPx
        );

        float contentW = contentArea.width();
        float contentH = contentArea.height();

        if (!isExporting) {
            computedTileRects.clear();
        }

        // 2. Draw Image Tiles
        for (int i = 0; i < count; i++) {
            if (i >= template.bounds.length || i >= tiles.size()) break;

            RectF normRect = template.bounds[i];
            TileState tile = tiles.get(i);
            if (tile.bitmap == null || tile.bitmap.isRecycled()) continue;

            float left = contentArea.left + normRect.left * contentW + (normRect.left > 0 ? innerPaddingPx / 2f : 0f);
            float top = contentArea.top + normRect.top * contentH + (normRect.top > 0 ? innerPaddingPx / 2f : 0f);
            float right = contentArea.left + normRect.right * contentW - (normRect.right < 1f ? innerPaddingPx / 2f : 0f);
            float bottom = contentArea.top + normRect.bottom * contentH - (normRect.bottom < 1f ? innerPaddingPx / 2f : 0f);

            RectF tileRect = new RectF(left, top, right, bottom);
            if (!isExporting) {
                computedTileRects.add(tileRect);
            }

            // Tile Shadow
            if (enableTileShadow && cornerRadiusPx > 0) {
                RectF shadowRect = new RectF(tileRect.left + dpToPx(2f), tileRect.top + dpToPx(2f),
                        tileRect.right + dpToPx(2f), tileRect.bottom + dpToPx(2f));
                canvas.drawRoundRect(shadowRect, cornerRadiusPx, cornerRadiusPx, shadowPaint);
            }

            canvas.save();
            clipPath.reset();
            clipPath.addRoundRect(tileRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW);
            canvas.clipPath(clipPath);

            drawTileBitmap(canvas, tile, tileRect);
            canvas.restore();

            // Highlight border if selected
            if (!isExporting && i == selectedTileIndex) {
                canvas.drawRoundRect(tileRect, cornerRadiusPx, cornerRadiusPx, highlightPaint);
            }
        }

        // 3. Draw Freehand Doodles
        for (DoodleStroke stroke : doodleStrokes) {
            doodlePaint.setColor(stroke.color);
            doodlePaint.setStrokeWidth(dpToPx(stroke.strokeWidthDp) * scaleFactor);
            canvas.drawPath(stroke.path, doodlePaint);
        }

        // 4. Draw Stickers
        for (StickerOverlay sticker : stickerOverlays) {
            stickerPaint.setTextSize(dpToPx(sticker.sizeDp) * scaleFactor);
            float cx = canvasBounds.left + sticker.x * canvasBounds.width();
            float cy = canvasBounds.top + sticker.y * canvasBounds.height();
            canvas.drawText(sticker.emoji, cx, cy, stickerPaint);
        }

        // 5. Draw Text Overlays
        for (TextOverlay text : textOverlays) {
            textPaint.setColor(text.textColor);
            textPaint.setTextSize(dpToPx(text.textSizeDp) * scaleFactor);

            float cx = canvasBounds.left + text.x * canvasBounds.width();
            float cy = canvasBounds.top + text.y * canvasBounds.height();

            Rect textBounds = new Rect();
            textPaint.getTextBounds(text.text, 0, text.text.length(), textBounds);

            float padH = dpToPx(12f) * scaleFactor;
            float padV = dpToPx(6f) * scaleFactor;
            RectF pillRect = new RectF(
                    cx - (textBounds.width() / 2f) - padH,
                    cy - textBounds.height() - padV,
                    cx + (textBounds.width() / 2f) + padH,
                    cy + padV
            );

            if (text.bgColor != Color.TRANSPARENT) {
                textBgPaint.setColor(text.bgColor);
                float pillRadius = pillRect.height() / 2f;
                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, textBgPaint);
            }

            canvas.drawText(text.text, cx, cy, textPaint);
        }
    }

    private void drawTileBitmap(Canvas canvas, TileState tile, RectF dstRect) {
        Bitmap bmp = tile.bitmap;
        if (bmp == null || bmp.isRecycled()) return;

        int bw = bmp.getWidth();
        int bh = bmp.getHeight();

        // Swap dimensions if 90 or 270 deg
        boolean rotated = (tile.rotationDegrees == 90 || tile.rotationDegrees == 270);
        int effW = rotated ? bh : bw;
        int effH = rotated ? bw : bh;

        float dw = dstRect.width();
        float dh = dstRect.height();

        float baseScale;
        if (effW * dh > dw * effH) {
            baseScale = dh / (float) effH;
        } else {
            baseScale = dw / (float) effW;
        }

        float finalScale = baseScale * tile.zoom;

        Matrix matrix = new Matrix();
        // 1. Center bitmap at origin
        matrix.postTranslate(-bw / 2f, -bh / 2f);

        // 2. Flips
        matrix.postScale(tile.flipH ? -1f : 1f, tile.flipV ? -1f : 1f);

        // 3. Rotation
        if (tile.rotationDegrees != 0) {
            matrix.postRotate(tile.rotationDegrees);
        }

        // 4. Scale
        matrix.postScale(finalScale, finalScale);

        // 5. Translate to tile center
        matrix.postTranslate(dstRect.centerX(), dstRect.centerY());

        // 6. Filter matrix
        if (tile.filter != null && tile.filter != ImageEnhancer.FilterType.NONE) {
            ColorMatrix cm = ImageEnhancer.getFilterMatrix(tile.filter);
            bitmapPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        } else {
            bitmapPaint.setColorFilter(null);
        }

        canvas.drawBitmap(bmp, matrix, bitmapPaint);
        bitmapPaint.setColorFilter(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (interactiveMode == InteractiveMode.DOODLE) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentDoodleStroke = new DoodleStroke(doodleColor, doodleStrokeWidthDp);
                    currentDoodleStroke.path.moveTo(x, y);
                    doodleStrokes.add(currentDoodleStroke);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (currentDoodleStroke != null) {
                        currentDoodleStroke.path.lineTo(x, y);
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    currentDoodleStroke = null;
                    return true;
            }
            return true;
        }

        // Dragging Text or Sticker Overlay
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            draggedOverlay = findOverlayAt(x, y);
            if (draggedOverlay != null) {
                dragStartTouch.set(x, y);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && draggedOverlay != null) {
            float normX = Math.max(0.05f, Math.min(0.95f, x / getWidth()));
            float normY = Math.max(0.05f, Math.min(0.95f, y / getHeight()));

            if (draggedOverlay instanceof TextOverlay) {
                ((TextOverlay) draggedOverlay).x = normX;
                ((TextOverlay) draggedOverlay).y = normY;
            } else if (draggedOverlay instanceof StickerOverlay) {
                ((StickerOverlay) draggedOverlay).x = normX;
                ((StickerOverlay) draggedOverlay).y = normY;
            }
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (draggedOverlay != null) {
                draggedOverlay = null;
                return true;
            }

            // Normal Tile Selection / Swap
            for (int i = 0; i < computedTileRects.size(); i++) {
                if (computedTileRects.get(i).contains(x, y)) {
                    handleTileClick(i);
                    return true;
                }
            }
        }

        return true;
    }

    private Object findOverlayAt(float x, float y) {
        float w = getWidth();
        float h = getHeight();

        // Check text overlays
        for (int i = textOverlays.size() - 1; i >= 0; i--) {
            TextOverlay t = textOverlays.get(i);
            float cx = t.x * w;
            float cy = t.y * h;
            if (Math.hypot(x - cx, y - cy) < dpToPx(40f)) {
                return t;
            }
        }

        // Check stickers
        for (int i = stickerOverlays.size() - 1; i >= 0; i--) {
            StickerOverlay s = stickerOverlays.get(i);
            float cx = s.x * w;
            float cy = s.y * h;
            if (Math.hypot(x - cx, y - cy) < dpToPx(35f)) {
                return s;
            }
        }
        return null;
    }

    private void handleTileClick(int clickedIndex) {
        if (selectedTileIndex == -1) {
            selectedTileIndex = clickedIndex;
            if (eventListener != null) {
                eventListener.onTileSelected(clickedIndex, tiles.get(clickedIndex));
            }
            invalidate();
        } else if (selectedTileIndex == clickedIndex) {
            selectedTileIndex = -1;
            if (eventListener != null) {
                eventListener.onTileDeselected();
            }
            invalidate();
        } else {
            // Swap tiles
            int first = selectedTileIndex;
            int second = clickedIndex;
            Collections.swap(tiles, first, second);
            selectedTileIndex = second; // Keep newly swapped tile selected
            if (eventListener != null) {
                eventListener.onTilesSwapped(first, second);
                eventListener.onTileSelected(second, tiles.get(second));
            }
            invalidate();
        }
    }

    public Bitmap renderBitmap(int targetWidth) {
        if (targetWidth <= 0) targetWidth = 2048;
        int targetHeight = (int) (targetWidth / currentRatio.getRatio());

        Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF bounds = new RectF(0, 0, targetWidth, targetHeight);
        drawCollage(canvas, bounds, true);
        return result;
    }

    public static List<Template> getAvailableTemplates(int count) {
        List<Template> list = new ArrayList<>();
        switch (count) {
            case 2:
                list.add(new Template("Vertical Split", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 1f, 1f)
                }));
                list.add(new Template("Horizontal Split", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("Focus Left (70/30)", new RectF[]{
                        new RectF(0f, 0f, 0.7f, 1f),
                        new RectF(0.7f, 0f, 1f, 1f)
                }));
                list.add(new Template("Focus Top (70/30)", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.7f),
                        new RectF(0f, 0.7f, 1f, 1f)
                }));
                break;

            case 3:
                list.add(new Template("1 Top, 2 Bottom", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.5f, 1f),
                        new RectF(0.5f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("2 Top, 1 Bottom", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 0.5f),
                        new RectF(0.5f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("1 Left, 2 Right", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 1f, 0.5f),
                        new RectF(0.5f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("2 Left, 1 Right", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 0.5f),
                        new RectF(0f, 0.5f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 1f, 1f)
                }));
                list.add(new Template("3 Columns", new RectF[]{
                        new RectF(0f, 0f, 0.333f, 1f),
                        new RectF(0.333f, 0f, 0.666f, 1f),
                        new RectF(0.666f, 0f, 1f, 1f)
                }));
                list.add(new Template("3 Rows", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.333f),
                        new RectF(0f, 0.333f, 1f, 0.666f),
                        new RectF(0f, 0.666f, 1f, 1f)
                }));
                break;

            case 4:
                list.add(new Template("2x2 Grid", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 0.5f),
                        new RectF(0.5f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.5f, 1f),
                        new RectF(0.5f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("1 Left, 3 Right", new RectF[]{
                        new RectF(0f, 0f, 0.6f, 1f),
                        new RectF(0.6f, 0f, 1f, 0.333f),
                        new RectF(0.6f, 0.333f, 1f, 0.666f),
                        new RectF(0.6f, 0.666f, 1f, 1f)
                }));
                list.add(new Template("1 Top, 3 Bottom", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.6f),
                        new RectF(0f, 0.6f, 0.333f, 1f),
                        new RectF(0.333f, 0.6f, 0.666f, 1f),
                        new RectF(0.666f, 0.6f, 1f, 1f)
                }));
                list.add(new Template("3 Left, 1 Right", new RectF[]{
                        new RectF(0f, 0f, 0.4f, 0.333f),
                        new RectF(0f, 0.333f, 0.4f, 0.666f),
                        new RectF(0f, 0.666f, 0.4f, 1f),
                        new RectF(0.4f, 0f, 1f, 1f)
                }));
                list.add(new Template("4 Columns", new RectF[]{
                        new RectF(0f, 0f, 0.25f, 1f),
                        new RectF(0.25f, 0f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 0.75f, 1f),
                        new RectF(0.75f, 0f, 1f, 1f)
                }));
                list.add(new Template("4 Rows", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.25f),
                        new RectF(0f, 0.25f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 1f, 0.75f),
                        new RectF(0f, 0.75f, 1f, 1f)
                }));
                break;

            case 5:
                list.add(new Template("2 Top, 3 Bottom", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 0.5f),
                        new RectF(0.5f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.333f, 1f),
                        new RectF(0.333f, 0.5f, 0.666f, 1f),
                        new RectF(0.666f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("3 Top, 2 Bottom", new RectF[]{
                        new RectF(0f, 0f, 0.333f, 0.5f),
                        new RectF(0.333f, 0f, 0.666f, 0.5f),
                        new RectF(0.666f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.5f, 1f),
                        new RectF(0.5f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("1 Left, 4 Right (2x2)", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 0.75f, 0.5f),
                        new RectF(0.75f, 0f, 1f, 0.5f),
                        new RectF(0.5f, 0.5f, 0.75f, 1f),
                        new RectF(0.75f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("1 Top, 4 Bottom", new RectF[]{
                        new RectF(0f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.25f, 1f),
                        new RectF(0.25f, 0.5f, 0.5f, 1f),
                        new RectF(0.5f, 0.5f, 0.75f, 1f),
                        new RectF(0.75f, 0.5f, 1f, 1f)
                }));
                break;

            case 6:
                list.add(new Template("2x3 Grid", new RectF[]{
                        new RectF(0f, 0f, 0.333f, 0.5f),
                        new RectF(0.333f, 0f, 0.666f, 0.5f),
                        new RectF(0.666f, 0f, 1f, 0.5f),
                        new RectF(0f, 0.5f, 0.333f, 1f),
                        new RectF(0.333f, 0.5f, 0.666f, 1f),
                        new RectF(0.666f, 0.5f, 1f, 1f)
                }));
                list.add(new Template("3x2 Grid", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 0.333f),
                        new RectF(0.5f, 0f, 1f, 0.333f),
                        new RectF(0f, 0.333f, 0.5f, 0.666f),
                        new RectF(0.5f, 0.333f, 1f, 0.666f),
                        new RectF(0f, 0.666f, 0.5f, 1f),
                        new RectF(0.5f, 0.666f, 1f, 1f)
                }));
                list.add(new Template("1 Left, 5 Right", new RectF[]{
                        new RectF(0f, 0f, 0.5f, 1f),
                        new RectF(0.5f, 0f, 0.75f, 0.5f),
                        new RectF(0.75f, 0f, 1f, 0.5f),
                        new RectF(0.5f, 0.5f, 0.666f, 1f),
                        new RectF(0.666f, 0.5f, 0.833f, 1f),
                        new RectF(0.833f, 0.5f, 1f, 1f)
                }));
                break;

            default:
                int cols = (int) Math.ceil(Math.sqrt(count));
                int rows = (int) Math.ceil((double) count / cols);
                RectF[] rects = new RectF[count];
                float cellW = 1f / cols;
                float cellH = 1f / rows;
                for (int i = 0; i < count; i++) {
                    int r = i / cols;
                    int c = i % cols;
                    rects[i] = new RectF(c * cellW, r * cellH, (c + 1) * cellW, (r + 1) * cellH);
                }
                list.add(new Template("Grid (" + cols + "x" + rows + ")", rects));
                break;
        }

        if (list.isEmpty()) {
            list.add(new Template("Default", new RectF[]{new RectF(0f, 0f, 1f, 1f)}));
        }
        return list;
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
