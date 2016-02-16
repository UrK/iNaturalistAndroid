package org.tatzpiteva.golan;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LaunchScreenCarouselManager implements LoaderManager.LoaderCallbacks<LaunchScreenCarouselConfig> {

    private static final String TAG = "LaunchScreenCM";
    private static final String CONFIG_CACHE_FILE = "launch_screen_cache";

    public interface ConfigRefreshListener {
        void onCarouselConfigRefresh(LaunchScreenCarouselConfig config);
    }

    private Context mContext;
    private ConfigRefreshListener listener;
    private LaunchScreenCarouselConfig mCachedConfig;

    // region Lifecycle

    public LaunchScreenCarouselManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mCachedConfig = loadCachedConfig();
    }

    // endregion

    // region Interface

    public void retrieveCarouselItems(Activity activity) {
        activity.getLoaderManager().initLoader(0, null, this).forceLoad();
        this.listener = null;
    }

    public void setOnConfigRefreshListener(ConfigRefreshListener listener) {
        this.listener = listener;
    }

    public LaunchScreenCarouselConfig getCachedConfig() {
        return mCachedConfig;
    }

    // endregion

    // region LoaderManager.LoaderCallbacks<>

    @Override
    public Loader<LaunchScreenCarouselConfig> onCreateLoader(int i, Bundle bundle) {
        return new LaunchScreenLoader(mContext, ConfigurationManager.getInstance().getAboutPicsUrl());
    }

    @Override
    public void onLoadFinished(Loader<LaunchScreenCarouselConfig> loader, @Nullable LaunchScreenCarouselConfig config) {

        if (config != null) {
            saveConfig(config);
            mCachedConfig = config;
        }

        if (listener != null) {
            listener.onCarouselConfigRefresh(config);
        }
    }

    @Override
    public void onLoaderReset(Loader<LaunchScreenCarouselConfig> loader) {
        this.mContext = null;
    }

    // endregion

    // region Utilities

    private void saveConfig(@NonNull LaunchScreenCarouselConfig config) {
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(CONFIG_CACHE_FILE, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Could not open cache file: ", e);
            return;
        }

        try {
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(config);
            os.close();
            Log.d(TAG, "Successfully saved cached carousel config");
        } catch (IOException e) {
            Log.w(TAG, "Could not write carousel config into cache file", e);
        } finally {
            try { outputStream.close(); }
            catch (IOException ignored) { }
        }
    }

    @Nullable
    private LaunchScreenCarouselConfig loadCachedConfig() {
        FileInputStream inputStream;

        try {
            inputStream = mContext.openFileInput(CONFIG_CACHE_FILE);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Carousel config cache file not found");
            return null;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(inputStream);
            final LaunchScreenCarouselConfig rv = (LaunchScreenCarouselConfig) is.readObject();

            Log.d(TAG, "Successfully loaded cached carousel config");

            return rv;
        } catch (ClassNotFoundException | IOException | ClassCastException e) {
            Log.e(TAG, "Invalid carousel config cache file", e);
            return null;
        }
    }

    // endregion
}
