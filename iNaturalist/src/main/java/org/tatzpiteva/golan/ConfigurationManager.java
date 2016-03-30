package org.tatzpiteva.golan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.inaturalist.android.INaturalistService;
import org.inaturalist.android.SerializableJSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Manager of application configuration
 */
public class ConfigurationManager {

    // region Constants

    private final static String TAG = "ConfigurationManager";
    private final static String API_SERVER_DEV = "http://golan.carmel.coop/json/";
    private final static String API_SERVER_PROD = "http://tatzpiteva.org.il/json/";
    private final static String API_SERVER_CURRENT = API_SERVER_PROD;

    private final static String API_ENDPOINT_PROJECTS = "projects";
    private final static String API_ENDPOINT_LAUNCH_SCREEN_CAROUSEL = "app/about";

    private final static String SHARED_PREFS_CONFIG = "GolanConfigurationManagerConfig";

    // endregion

    // region helper classes and interfaces

    private class ProjectDetailsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* this is different get request, just drop it and continue waiting for the one issued by this instance */

            int returnedProjectId = intent.getExtras().getInt(INaturalistService.PROJECT_ID);
            Config.AutoProject autoProject = ConfigHelper.findAutoProject(returnedProjectId);

            if (returnedProjectId != getConfig().getAutoUserJoinProject() && autoProject == null) {
                return;
            }

            SerializableJSONArray projs =
                    (SerializableJSONArray) intent.getExtras().getSerializable(INaturalistService.PROJECTS_RESULT);

            if (projs == null || projs.getJSONArray() == null || projs.getJSONArray().length() == 0) {
                // TODO: inform about an error
                return;
            }

            try {
                JSONObject projDetails = projs.getJSONArray().getJSONObject(0);

                if (getConfig().getAutoUserJoinProject() == returnedProjectId) {
                    ConfigurationManager.this.getConfig().setAutoUserJoinProjectDetails(projDetails);
                }

                if (ConfigHelper.addDetailsToProject(returnedProjectId, projDetails)) {
                    Log.v(TAG, "Successfully added details to project " + returnedProjectId);
                } else {
                    Log.i(TAG, "Failed to add details to project " + returnedProjectId);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Unable to retrieve auto-project configuration", e);
            }

            /* if all has been retrieved: auto user join project and all the auto-projects, unregister the receiver, as
             * there is no need to listen for project details retrieval events */
            if (getConfig().getAutoUserJoinProjectDetails() != null && ConfigHelper.allAutoProjectHaveDetails()) {
                context.unregisterReceiver(this);
            }
        }
    }

    // endregion

    // region Properties

    @Nullable
    private static ConfigurationManager mInstance;

    @Nullable
    private Config config;

    @Nullable
    private Context context;

    // endregion

    // region Lifecycle

    private ConfigurationManager(@SuppressWarnings("NullableProblems") @NonNull Context context) {
        this.context = context;
        this.config = loadCachedConfig();

        ConfigDownloadTask.OnCompleteListener mDownloadListener = new ConfigDownloadTask.OnCompleteListener() {
            @Override
            public void onConfigDownloadComplete(@Nullable Config config, @Nullable String stringData) {
                handleDownload(config, stringData);
            }
        };

        new ConfigDownloadTask(mDownloadListener).execute(API_SERVER_CURRENT + API_ENDPOINT_PROJECTS);
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
            retrieveAutoUserJoinProjectDetails();
            retrieveAutoProjectDetails();
        }
    }

    private void retrieveAutoUserJoinProjectDetails() {
        if (context == null) {
            return;
        }

        context.registerReceiver(
                new ProjectDetailsReceiver(),
                new IntentFilter(INaturalistService.ACTION_GET_PROJECT_DETAILS_RESULT));

        Intent serviceIntent = new Intent(
                INaturalistService.ACTION_GET_PROJECT_DETAILS, null, context, INaturalistService.class);

        serviceIntent.putExtra(
                INaturalistService.PROJECT_ID,
                ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject());

        context.startService(serviceIntent);
    }

    private void retrieveAutoProjectDetails() {
        if (context == null || config == null || config.getAutoProjects() == null) {
            return;
        }

        context.registerReceiver(
                new ProjectDetailsReceiver(),
                new IntentFilter(INaturalistService.ACTION_GET_PROJECT_DETAILS_RESULT));

        for (Config.AutoProject p : config.getAutoProjects()) {
            Intent serviceIntent = new Intent(
                    INaturalistService.ACTION_GET_PROJECT_DETAILS, null, context, INaturalistService.class);

            serviceIntent.putExtra(INaturalistService.PROJECT_ID, p.id);

            context.startService(serviceIntent);
        }
    }

    private void cacheConfig(@NonNull String stringData) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFS_CONFIG, stringData);
        editor.apply();
    }

    private Config loadCachedConfig() {
        if (context == null) {
            return null;
        }
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

    // region Getters/setters

    @NonNull
    public Config getConfig() {
        return config != null ? config : new Config();
    }

    public String getAboutPicsUrl() {
        return API_SERVER_CURRENT + API_ENDPOINT_LAUNCH_SCREEN_CAROUSEL;
    }

    // endregion
}

