package com.example.smslinkopener;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST = 100;
    private static final String PREFS_NAME = "SmsLinkPrefs";

    // SharedPreferences keys
    public static final String KEY_TRIGGER_KEYWORD = "trigger_keyword";
    public static final String KEY_TARGET_URL      = "target_url";
    public static final String KEY_SENDER_FILTER   = "sender_filter";
    public static final String KEY_ENABLED         = "enabled";

    private EditText etKeyword, etUrl, etSender;
    private Switch   swEnabled;
    private TextView tvStatus, tvLastSms;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs    = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etKeyword = findViewById(R.id.etKeyword);
        etUrl     = findViewById(R.id.etUrl);
        etSender  = findViewById(R.id.etSender);
        swEnabled = findViewById(R.id.swEnabled);
        tvStatus  = findViewById(R.id.tvStatus);
        tvLastSms = findViewById(R.id.tvLastSms);

        // Load saved settings
        etKeyword.setText(prefs.getString(KEY_TRIGGER_KEYWORD, "OTVORI"));
        etUrl.setText(prefs.getString(KEY_TARGET_URL, "http://192.168.1.1/action"));
        etSender.setText(prefs.getString(KEY_SENDER_FILTER, ""));
        swEnabled.setChecked(prefs.getBoolean(KEY_ENABLED, true));

        // Save button
        findViewById(R.id.btnSave).setOnClickListener(v -> saveSettings());

        // Test button – opens the URL immediately
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty()) openUrl(url);
            else Toast.makeText(this, "Unesi URL prvo!", Toast.LENGTH_SHORT).show();
        });

        checkPermissions();
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh last SMS info set by the receiver
        String last = prefs.getString("last_sms_info", "Nema primljenih SMS poruka još.");
        tvLastSms.setText(last);
    }

    private void saveSettings() {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_TRIGGER_KEYWORD, etKeyword.getText().toString().trim());
        ed.putString(KEY_TARGET_URL,      etUrl.getText().toString().trim());
        ed.putString(KEY_SENDER_FILTER,   etSender.getText().toString().trim());
        ed.putBoolean(KEY_ENABLED,        swEnabled.isChecked());
        ed.apply();
        Toast.makeText(this, "✅ Postavke spremljene!", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void updateStatus() {
        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);
        String  keyword = prefs.getString(KEY_TRIGGER_KEYWORD, "");
        String  url     = prefs.getString(KEY_TARGET_URL, "");
        String  sender  = prefs.getString(KEY_SENDER_FILTER, "");

        StringBuilder sb = new StringBuilder();
        sb.append(enabled ? "🟢 AKTIVNO" : "🔴 UGAŠENO").append("\n");
        if (!keyword.isEmpty()) sb.append("Ključna riječ: \"").append(keyword).append("\"\n");
        if (!sender.isEmpty())  sb.append("Pošiljatelj: ").append(sender).append("\n");
        if (!url.isEmpty())     sb.append("URL: ").append(url);
        tvStatus.setText(sb.toString());
    }

    public void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ne mogu otvoriti URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── Permissions ──────────────────────────────────────────────────────────

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                    }, SMS_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == SMS_PERMISSION_REQUEST) {
            boolean granted = res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED;
            Toast.makeText(this,
                    granted ? "✅ SMS dozvola odobrena!" : "❌ SMS dozvola odbijena – aplikacija neće raditi.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
