package org.tatzpiteva.golan;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.sanselan.util.IOUtils;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration download task
 */
public class ConfigDownloadTask extends AsyncTask<String, Void, Config> {

    // region Constants

    private final static String TAG = "ConfigDownloadTask";

    // endregion

    // region Helper classes and interfaces

    public interface OnCompleteListener {
        void onConfigDownloadComplete(@Nullable Config config, @Nullable String stringData);
    }

    // endregion

    // region Properties

    private final OnCompleteListener mListener;
    private String stringData;

    // endregion

    public ConfigDownloadTask(OnCompleteListener listener) {
        this.mListener = listener;
    }

    @Override
    protected Config doInBackground(String... urls) {
        URL url;

        Log.d(TAG, "Downloading remote configuration from " + urls[0]);

        try {
            url = new URL(urls[0]);
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Invalid API server URL: " + urls[0]);
            return null;
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
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
            return ConfigParser.parseConfig(stringData);
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

    @Override
    protected void onPostExecute(Config config) {
        super.onPostExecute(config);

        Log.d(TAG, "Remote configuration downloaded: " + config);

        /* no listener, nothing to do here */
        if (mListener == null) {
            return;
        }

        mListener.onConfigDownloadComplete(config, stringData);
    }
}
