package org.tatzpiteva.golan;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public class LaunchScreenCarouselManager implements LoaderManager.LoaderCallbacks<LaunchScreenCarouselConfig> {

    public interface ConfigRefreshListener {
        void onCarouselConfigRefresh(LaunchScreenCarouselConfig config);
    }

    private Context context;
    private ConfigRefreshListener listener;

    // region Interface

    public void retrieveCarouselItems(Activity activity) {
        this.context = activity;
        activity.getLoaderManager().initLoader(0, null, this).forceLoad();
        this.listener = null;
    }

    public void setOnConfigRefreshListener(ConfigRefreshListener listener) {
        this.listener = listener;
    }

    // endregion

    // region LoaderManager.LoaderCallbacks<>

    @Override
    public Loader<LaunchScreenCarouselConfig> onCreateLoader(int i, Bundle bundle) {
        return new LaunchScreenLoader(context, ConfigurationManager.getInstance().getAboutPicsUrl());
    }

    @Override
    public void onLoadFinished(Loader<LaunchScreenCarouselConfig> loader, LaunchScreenCarouselConfig config) {
        this.context = null;

        if (listener != null) {
            listener.onCarouselConfigRefresh(config);
        }
    }

    @Override
    public void onLoaderReset(Loader<LaunchScreenCarouselConfig> loader) {
        this.context = null;
    }

    // endregion
}
