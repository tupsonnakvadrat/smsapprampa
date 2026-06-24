package com.example.smslinkopener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG       = "SmsLinkOpener";
    private static final String PREFS     = "SmsLinkPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(MainActivity.KEY_ENABLED, true);
        if (!enabled) {
            Log.d(TAG, "App is disabled, ignoring SMS.");
            return;
        }

        String keyword      = prefs.getString(MainActivity.KEY_TRIGGER_KEYWORD, "").trim();
        String targetUrl    = prefs.getString(MainActivity.KEY_TARGET_URL, "").trim();
        String senderFilter = prefs.getString(MainActivity.KEY_SENDER_FILTER, "").trim();

        // Parse SMS messages from the bundle
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String   format = bundle.getString("format");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage msg;
            try {
                if (format != null) {
                    msg = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    msg = SmsMessage.createFromPdu((byte[]) pdu);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse PDU", e);
                continue;
            }

            String sender  = msg.getOriginatingAddress();
            String body    = msg.getMessageBody();

            if (sender == null || body == null) continue;

            Log.d(TAG, "SMS from: " + sender + " | body: " + body);

            // ── Sender filter (optional) ──────────────────────────────────────
            if (!senderFilter.isEmpty() && !sender.contains(senderFilter)) {
                Log.d(TAG, "Sender doesn't match filter, skipping.");
                continue;
            }

            // ── Keyword check ─────────────────────────────────────────────────
            boolean keywordMatch = keyword.isEmpty()
                    || body.toUpperCase(Locale.ROOT).contains(keyword.toUpperCase(Locale.ROOT));

            if (!keywordMatch) {
                Log.d(TAG, "Keyword not found, skipping.");
                continue;
            }

            // ── Save last SMS info for UI ─────────────────────────────────────
            String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT)
                    .format(new Date());
            String info = "Posljednji okidač:\n" + timestamp
                    + "\nOd: " + sender
                    + "\nPoruka: " + body
                    + "\nOtvoren URL: " + targetUrl;
            prefs.edit().putString("last_sms_info", info).apply();

            // ── Open the URL ──────────────────────────────────────────────────
            if (!targetUrl.isEmpty()) {
                openUrl(context, targetUrl);
                Log.d(TAG, "Triggered URL: " + targetUrl);
            } else {
                Log.w(TAG, "No URL configured.");
            }
        }
    }

    private void openUrl(Context context, String url) {
        try {
            android.net.Uri uri = android.net.Uri.parse(url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open URL: " + url, e);
        }
    }
}
