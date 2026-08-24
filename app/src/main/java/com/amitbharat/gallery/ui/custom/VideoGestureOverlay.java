package com.amitbharat.gallery.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amitbharat.gallery.R;

public class VideoGestureOverlay extends FrameLayout implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public interface VideoGestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onSeek(long deltaMillis);
        long getCurrentPosition();
        long getDuration();
    }

    private GestureDetector gestureDetector;
    private VideoGestureListener listener;
    private AudioManager audioManager;

    private View indicatorContainer;
    private ImageView indicatorIcon;
    private ProgressBar indicatorProgress;
    private TextView indicatorText;

    private int maxVolume;
    private float currentBrightness = -1;
    private boolean isVolumeGesture = false;
    private boolean isBrightnessGesture = false;
    private boolean isSeekGesture = false;
    private float initialTouchX = 0;

    public VideoGestureOverlay(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoGestureOverlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
    }

    public void setIndicatorViews(View container, ImageView icon, ProgressBar progress, TextView text) {
        this.indicatorContainer = container;
        this.indicatorIcon = icon;
        this.indicatorProgress = progress;
        this.indicatorText = text;
    }

    public void setVideoGestureListener(VideoGestureListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            isVolumeGesture = false;
            isBrightnessGesture = false;
            isSeekGesture = false;
            if (indicatorContainer != null) {
                indicatorContainer.setVisibility(View.GONE);
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        initialTouchX = e.getX();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e1 == null || e2 == null) return false;

        float deltaX = e2.getX() - e1.getX();
        float deltaY = e1.getY() - e2.getY(); // Upward is positive
        int width = getWidth();
        int height = getHeight();

        if (!isVolumeGesture && !isBrightnessGesture && !isSeekGesture) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                isSeekGesture = true;
            } else {
                if (e1.getX() < width / 2f) {
                    isBrightnessGesture = true;
                } else {
                    isVolumeGesture = true;
                }
            }
        }

        if (isBrightnessGesture) {
            adjustBrightness(deltaY / (float) height);
            return true;
        } else if (isVolumeGesture) {
            adjustVolume(deltaY / (float) height);
            return true;
        } else if (isSeekGesture) {
            if (listener != null) {
                long deltaMillis = (long) ((deltaX / (float) width) * 90000); // 90 sec max seek range per swipe
                listener.onSeek(deltaMillis);
            }
            return true;
        }

        return false;
    }

    private void adjustVolume(float percentDelta) {
        if (audioManager == null || getContext() == null) return;
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int change = (int) (percentDelta * maxVolume);
        int target = Math.max(0, Math.min(maxVolume, currentVolume + change));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);

        if (indicatorContainer != null) {
            indicatorContainer.setVisibility(View.VISIBLE);
            indicatorProgress.setMax(maxVolume);
            indicatorProgress.setProgress(target);
            indicatorText.setText(String.format("%d%%", (target * 100) / maxVolume));
        }
    }

    private void adjustBrightness(float percentDelta) {
        if (!(getContext() instanceof Activity)) return;
        Activity activity = (Activity) getContext();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if (currentBrightness < 0) {
            currentBrightness = lp.screenBrightness >= 0 ? lp.screenBrightness : 0.5f;
        }
        currentBrightness = Math.max(0.01f, Math.min(1.0f, currentBrightness + (percentDelta * 0.05f)));
        lp.screenBrightness = currentBrightness;
        activity.getWindow().setAttributes(lp);

        if (indicatorContainer != null) {
            indicatorContainer.setVisibility(View.VISIBLE);
            indicatorProgress.setMax(100);
            indicatorProgress.setProgress((int) (currentBrightness * 100));
            indicatorText.setText(String.format("%d%%", (int) (currentBrightness * 100)));
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (listener != null) {
            listener.onSingleTap();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (listener != null) {
            listener.onDoubleTap();
        }
        return true;
    }

    @Override public void onShowPress(MotionEvent e) {}
    @Override public boolean onSingleTapUp(MotionEvent e) { return false; }
    @Override public void onLongPress(MotionEvent e) {}
    @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
    @Override public boolean onDoubleTapEvent(MotionEvent e) { return false; }
}
