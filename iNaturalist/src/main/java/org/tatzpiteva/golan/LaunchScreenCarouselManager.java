package org.tatzpiteva.golan;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public class LaunchScreenCarouselManager implements LoaderManager.LoaderCallbacks<LaunchScreenCarouselConfig> {

    private Context context;

    private void retrieveCarouselItems(Activity activity) {
        activity.getLoaderManager().initLoader(0, null, this);
        this.context = activity;
    }

    // region LoaderManager.LoaderCallbacks<>

    @Override
    public Loader<LaunchScreenCarouselConfig> onCreateLoader(int i, Bundle bundle) {
        return new Loader<>(context);
    }

    @Override
    public void onLoadFinished(Loader<LaunchScreenCarouselConfig> loader, LaunchScreenCarouselConfig launchScreenCarouselConfig) {
    }

    @Override
    public void onLoaderReset(Loader<LaunchScreenCarouselConfig> loader) {

    }

    // endregion
}
