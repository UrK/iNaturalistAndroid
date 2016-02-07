package org.tatzpiteva.golan;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.sanselan.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LaunchScreenLoader extends AsyncTaskLoader<LaunchScreenCarouselConfig> {
    public static final String TAG = "LaunchScreenLoader";

    @NonNull
    private final String url;

    private String stringData;

    public LaunchScreenLoader(@NonNull Context context, @NonNull String url) {
        super(context);
        this.url = url;
    }

    @Override
    public LaunchScreenCarouselConfig loadInBackground() {
        Log.d(TAG, "Downloading remote configuration from " + this.url);

        URL downloadUrl;

        try {
            downloadUrl = new URL(this.url);
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Invalid API server URL: " + this.url);
            return null;
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) downloadUrl.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open connection to configuration server: " + e.getMessage());
            return null;
        }

        try {
            if (connection.getResponseCode() != 200) {
                Log.e(TAG, "Failed to retrieve configuration: invalid status code " + connection.getResponseCode());
                return null;
            }


            ByteArrayOutputStream jsonBuffer = new ByteArrayOutputStream();
            IOUtils.copyStreamToStream(connection.getInputStream(), jsonBuffer);
            stringData = jsonBuffer.toString("UTF-8");
            return LaunchScreenConfigParser.parseConfig(stringData);
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrieve configuration: " + e.getMessage());
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON configuration file: " + e.getMessage());
            return null;
        } finally {
            connection.disconnect();
        }
    }

}
