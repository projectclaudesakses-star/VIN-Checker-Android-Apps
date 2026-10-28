package com.vinchecker.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * VIN Checker untuk Android.
 * Aplikasi ini hanya membuka website VIN Checker di GitHub Pages,
 * jadi kalau tampilan website diubah, aplikasi ikut berubah tanpa update.
 */
public class MainActivity extends Activity {

    // ====== Alamat website VIN Checker (GitHub Pages) ======
    private static final String START_URL = "https://projectclaudesakses-star.github.io/VIN-Checker/";

    private static final String HOST = Uri.parse(START_URL).getHost();
    private WebView web;
    private LinearLayout errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#263445"));

        // ---- Halaman website ----
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (HOST != null && HOST.equalsIgnoreCase(uri.getHost())) {
                    return false; // tetap di dalam aplikasi
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri)); // link lain dibuka di browser
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    showError(true);
                }
            }
        });
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ---- Tampilan kalau tidak ada internet ----
        errorView = new LinearLayout(this);
        errorView.setOrientation(LinearLayout.VERTICAL);
        errorView.setGravity(Gravity.CENTER);
        errorView.setBackgroundColor(Color.parseColor("#F4F5F7"));
        int pad = dp(32);
        errorView.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Tidak ada koneksi internet");
        title.setTextSize(20);
        title.setTextColor(Color.parseColor("#263445"));
        title.setGravity(Gravity.CENTER);

        TextView sub = new TextView(this);
        sub.setText("Periksa koneksi Anda, lalu coba lagi.");
        sub.setTextSize(15);
        sub.setTextColor(Color.parseColor("#5F6670"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(8), 0, dp(24));

        Button retry = new Button(this);
        retry.setText("Coba lagi");
        retry.setOnClickListener(v -> {
            showError(false);
            web.loadUrl(START_URL);
        });

        errorView.addView(title);
        errorView.addView(sub);
        errorView.addView(retry);
        errorView.setVisibility(View.GONE);
        root.addView(errorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Android 15+: konten tidak boleh tertutup status bar / keyboard
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets b = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout()
                        | WindowInsets.Type.ime());
                v.setPadding(b.left, b.top, b.right, b.bottom);
                return WindowInsets.CONSUMED;
            });
        }

        setContentView(root);

        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            web.loadUrl(START_URL);
        }
    }

    private void showError(boolean show) {
        errorView.setVisibility(show ? View.VISIBLE : View.GONE);
        web.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
        }
        super.onDestroy();
    }
}
