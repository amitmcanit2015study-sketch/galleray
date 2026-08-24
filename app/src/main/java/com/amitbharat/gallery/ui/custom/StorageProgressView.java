package com.amitbharat.gallery.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.StorageCategory;
import java.util.ArrayList;
import java.util.List;

public class StorageProgressView extends View {
    private final List<StorageCategory> categories = new ArrayList<>();
    private long totalStorageBytes = 1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float cornerRadius = 12f;

    public StorageProgressView(Context context) {
        super(context);
        init();
    }

    public StorageProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cornerRadius = getResources().getDisplayMetrics().density * 8;
    }

    public void setStorageData(List<StorageCategory> list, long totalBytes) {
        this.categories.clear();
        if (list != null) {
            this.categories.addAll(list);
        }
        this.totalStorageBytes = Math.max(1, totalBytes);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        // Draw background (Free space)
        paint.setColor(ContextCompat.getColor(getContext(), R.color.cat_free));
        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        if (categories.isEmpty() || totalStorageBytes <= 0) return;

        float currentX = 0;
        for (StorageCategory cat : categories) {
            if (cat.getTotalBytes() <= 0) continue;

            float segmentWidth = ((float) cat.getTotalBytes() / totalStorageBytes) * width;
            if (segmentWidth < 2f) segmentWidth = 2f;

            paint.setColor(ContextCompat.getColor(getContext(), cat.getColorRes()));

            rect.set(currentX, 0, currentX + segmentWidth, height);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

            currentX += segmentWidth;
            if (currentX >= width) break;
        }
    }
}
