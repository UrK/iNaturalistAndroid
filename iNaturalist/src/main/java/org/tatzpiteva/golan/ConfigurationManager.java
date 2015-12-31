package org.tatzpiteva.golan;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

/**
 * Manager of application configuration
 */
public class ConfigurationManager {

    // region Constants

    private final static String TAG = "ConfigurationManager";
    private final static String API_SERVER_DEV = "http://golan.carmel.coop/json/projects";
    private final static String API_SERVER_PROD = "http://tatzpiteva.org.il/json/projects";
    private final static String API_SERVER_CURRENT = API_SERVER_DEV;

    private final static String SHARED_PREFS_CONFIG = "GolanConfigurationManagerConfig";

    // endregion

    // region helper classes and interfaces

    // endregion

    // region Properties

    private static ConfigurationManager mInstance;

    private Config config;

    private Context context;

    // endregion

    // region Lifecycle

    private ConfigurationManager(Context context) {
        this.context = context;
        this.config = loadCachedConfig();

        ConfigDownloadTask.OnCompleteListener mDownloadListener = new ConfigDownloadTask.OnCompleteListener() {
            @Override
            public void onConfigDownloadComplete(@Nullable Config config, @Nullable String stringData) {
                handleDownload(config, stringData);
            }
        };

        new ConfigDownloadTask(mDownloadListener).execute(API_SERVER_CURRENT);
    }

    public static ConfigurationManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(
                    "Should call getInstance(Context) at least once before using getInstance()");
        }
        return mInstance;
    }

    public static ConfigurationManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ConfigurationManager.class) {
                if (mInstance == null) {
                    mInstance = new ConfigurationManager(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    protected void finalize() throws Throwable {
        if (context != null) {
            Log.e(TAG, "ConfigurationManager.getInstance().destroy() was not been called before shutdown." +
                    " Possible memory leak");
        }
        super.finalize();
    }

    // endregion

    // region Utilities

    private void handleDownload(@Nullable Config config, @Nullable String stringData) {
        if (config != null) {
            if (stringData != null) {
                cacheConfig(stringData);
            }
            this.config = config;
        }
    }

    private void cacheConfig(@NonNull String stringData) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFS_CONFIG, stringData);
        editor.apply();
    }

    private Config loadCachedConfig() {
        String strConfig = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(SHARED_PREFS_CONFIG, null);
        if (strConfig != null) {
            try {
                Config rv = ConfigParser.parseConfig(strConfig);
                Log.d(TAG, "Loaded cached configuration");
                return rv;
            } catch (IOException | JSONException e) {
                return null;
            }
        }
        return null;
    }

    // endregion
}

