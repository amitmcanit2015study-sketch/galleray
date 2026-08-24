package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.amitbharat.gallery.databinding.ActivityAboutBinding;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.util.concurrent.Executors;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    private static final String APP_ABOUT_TEXT = "Gallery - Modern Gallery & File Manager\n\n"
            + "Gallery is a free, ad-free gallery and file manager designed for a fast, simple, and seamless experience. Easily organize, browse, and manage your photos and files with a clean interface, smooth performance, and privacy at its core.\n\n"
            + "• Developed by: Amit Bharat\n"
            + "• Company: Rooys Soft Tech\n"
            + "• Contact: rooyssofttech2020@gmail.com\n"
            + "• Version: 1.0.0\n\n"
            + "Install the attached APK to get started!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupActions();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupActions() {
        binding.btnShareApp.setOnClickListener(v -> shareAppApk());
        binding.btnFeedback.setOnClickListener(v -> sendFeedbackEmail());
        binding.tvEmail.setOnClickListener(v -> sendFeedbackEmail());
    }

    private void shareAppApk() {
        Toast.makeText(this, "Preparing Gallery APK to share...", Toast.LENGTH_SHORT).show();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApplicationInfo appInfo = getApplicationInfo();
                File originalApk = new File(appInfo.sourceDir);

                if (!originalApk.exists()) {
                    runOnUiThread(() -> shareAppDescriptionFallback());
                    return;
                }

                // Copy to cache dir with a clean, branded APK file name
                File shareDir = new File(getCacheDir(), "shared_apk");
                if (!shareDir.exists()) {
                    shareDir.mkdirs();
                }
                File targetApk = new File(shareDir, "Gallery_by_AmitBharat.apk");
                FileUtils.copyFile(originalApk, targetApk);

                Uri apkUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        targetApk
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/vnd.android.package-archive");
                shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Gallery APK - by Amit Bharat (Rooys Soft Tech)");
                shareIntent.putExtra(Intent.EXTRA_TEXT, APP_ABOUT_TEXT);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> {
                    startActivity(Intent.createChooser(shareIntent, "Share Gallery APK & Details"));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sharing description...", Toast.LENGTH_SHORT).show();
                    shareAppDescriptionFallback();
                });
            }
        });
    }

    private void shareAppDescriptionFallback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Gallery - Modern Gallery & File Manager");
        intent.putExtra(Intent.EXTRA_TEXT, APP_ABOUT_TEXT);
        startActivity(Intent.createChooser(intent, "Share Gallery App"));
    }

    private void sendFeedbackEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:rooyssofttech2020@gmail.com"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rooyssofttech2020@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Gallery App - Feedback & Support");
        String body = "Hello Rooys Soft Tech Team,\n\n"
                + "Feedback / Feature Request / Bug Report:\n\n\n"
                + "------------------------------\n"
                + "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + "\n"
                + "Android: " + android.os.Build.VERSION.RELEASE + " (API " + android.os.Build.VERSION.SDK_INT + ")\n"
                + "App Version: 1.0.0\n";
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception ignored) {}
    }
}
