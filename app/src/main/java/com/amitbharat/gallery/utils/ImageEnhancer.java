package com.amitbharat.gallery.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageEnhancer {

    public enum FilterType {
        NONE,
        AUTO_ENHANCE,
        VIVID,
        BW,
        WARM,
        COOL,
        VINTAGE,
        DRAMATIC
    }

    public static ColorMatrix createAdjustmentMatrix(float brightness, float contrast, float saturation, float warmth) {
        ColorMatrix matrix = new ColorMatrix();

        // 1. Saturation (-100 to 100 -> 0.0 to 2.0)
        float satVal = 1.0f + (saturation / 100.0f);
        if (satVal < 0) satVal = 0;
        matrix.setSaturation(satVal);

        // 2. Contrast & Brightness
        // contrast scale: 0.5 to 2.0
        float scale = 1.0f + (contrast / 100.0f);
        if (scale < 0.1f) scale = 0.1f;
        float translate = brightness * 1.5f + (1.0f - scale) * 128f;

        ColorMatrix cbMatrix = new ColorMatrix(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        matrix.postConcat(cbMatrix);

        // 3. Warmth / Temperature (-100 cool/blue to +100 warm/amber)
        if (warmth != 0) {
            float rOffset = warmth * 0.5f;
            float bOffset = -warmth * 0.5f;
            ColorMatrix tempMatrix = new ColorMatrix(new float[]{
                    1, 0, 0, 0, rOffset,
                    0, 1, 0, 0, 0,
                    0, 0, 1, 0, bOffset,
                    0, 0, 0, 1, 0
            });
            matrix.postConcat(tempMatrix);
        }

        return matrix;
    }

    public static ColorMatrix getFilterMatrix(FilterType filter) {
        ColorMatrix cm = new ColorMatrix();
        switch (filter) {
            case AUTO_ENHANCE:
                // Boost brightness, contrast, and saturation
                ColorMatrix aeSat = new ColorMatrix();
                aeSat.setSaturation(1.25f);
                ColorMatrix aeCont = new ColorMatrix(new float[]{
                        1.15f, 0, 0, 0, -10,
                        0, 1.15f, 0, 0, -10,
                        0, 0, 1.15f, 0, -10,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(aeSat);
                cm.postConcat(aeCont);
                break;

            case VIVID:
                // Rich, punchy, vibrant colors
                ColorMatrix vividSat = new ColorMatrix();
                vividSat.setSaturation(1.65f);
                ColorMatrix vividCont = new ColorMatrix(new float[]{
                        1.22f, 0, 0, 0, -15,
                        0, 1.22f, 0, 0, -15,
                        0, 0, 1.22f, 0, -15,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(vividSat);
                cm.postConcat(vividCont);
                break;

            case BW:
                // Classic High-contrast B&W
                cm.setSaturation(0f);
                ColorMatrix bwCont = new ColorMatrix(new float[]{
                        1.25f, 0, 0, 0, -15,
                        0, 1.25f, 0, 0, -15,
                        0, 0, 1.25f, 0, -15,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(bwCont);
                break;

            case WARM:
                // Golden Hour Glow
                ColorMatrix warmMat = new ColorMatrix(new float[]{
                        1.20f, 0, 0, 0, 25,
                        0, 1.08f, 0, 0, 10,
                        0, 0, 0.85f, 0, -18,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(warmMat);
                break;

            case COOL:
                // Crisp Nordic Blue
                ColorMatrix coolMat = new ColorMatrix(new float[]{
                        0.85f, 0, 0, 0, -15,
                        0, 1.05f, 0, 0, 8,
                        0, 0, 1.30f, 0, 30,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(coolMat);
                break;

            case VINTAGE:
                // Warm retro film / sepia tone
                ColorMatrix vintageMat = new ColorMatrix(new float[]{
                        0.90f, 0.40f, 0.15f, 0, 25,
                        0.25f, 0.80f, 0.15f, 0, 15,
                        0.15f, 0.30f, 0.60f, 0, 10,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(vintageMat);
                break;

            case DRAMATIC:
                // Moody cinematic look
                ColorMatrix dramSat = new ColorMatrix();
                dramSat.setSaturation(0.70f);
                ColorMatrix dramCont = new ColorMatrix(new float[]{
                        1.40f, 0, 0, 0, -30,
                        0, 1.40f, 0, 0, -30,
                        0, 0, 1.40f, 0, -30,
                        0, 0, 0, 1, 0
                });
                cm.postConcat(dramSat);
                cm.postConcat(dramCont);
                break;

            case NONE:
            default:
                break;
        }
        return cm;
    }

    public static Bitmap applyEnhancements(Bitmap src, float brightness, float contrast, float saturation, float warmth, FilterType filter) {
        if (src == null) return null;

        ColorMatrix baseMatrix = createAdjustmentMatrix(brightness, contrast, saturation, warmth);
        if (filter != null && filter != FilterType.NONE) {
            ColorMatrix filterMatrix = getFilterMatrix(filter);
            baseMatrix.postConcat(filterMatrix);
        }

        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(baseMatrix));
        canvas.drawBitmap(src, 0, 0, paint);

        return output;
    }
}
