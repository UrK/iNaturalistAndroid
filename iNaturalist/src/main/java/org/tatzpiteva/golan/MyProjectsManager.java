package org.tatzpiteva.golan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.inaturalist.android.INaturalistPrefsActivity;
import org.inaturalist.android.INaturalistService;
import org.inaturalist.android.SerializableJSONArray;
import org.inaturalist.android.SignInTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Manager of own user projects
 */
public class MyProjectsManager {

    // region Constants

    private static final String TAG = "MyProjectsManager";
    public static final String ACTION_MY_PROJECTS_LOADED = "MyProjectsManager_ACTION_MY_PROJECTS_LOADED";

    public static final String ACTION_MY_PROJECTS_LOADING_STARTED =
            "MyProjectsManager_ACTION_MY_PROJECTS_LOADING_STARTED";

    private static final String PROJECTS_CACHE = "projects_cache";

    private boolean mIsLoading = false;

    // endregion

    // region Helper classes

    public static class Project implements Serializable {
        public int id;
        public String title;
        public Double latitude;
        public Double longitude;
        public Float zoomLevel;

        @Override
        public boolean equals(Object o) {
            return (o instanceof Project) && (this.id == ((Project) o).id);
        }
    }

    private class MyProjectsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            context.unregisterReceiver(this);

            context.registerReceiver(
                    new ProjectDetailsReceiver(),
                    new IntentFilter(INaturalistService.ACTION_GET_PROJECT_DETAILS_RESULT));

            SerializableJSONArray serializableArray =
                    (SerializableJSONArray) intent.getSerializableExtra(INaturalistService.PROJECTS_RESULT);

            if (serializableArray == null) {
                Log.w(TAG, "Invalid projects list (null)");
                return;
            }

            JSONArray projectArray = serializableArray.getJSONArray();

            downloadedProjects = new HashSet<>(projectArray.length());

            Log.d(TAG, "Retrieved " + projectArray.length() + " joined projects for user");

            for (int i = 0; i < projectArray.length(); i++) {
                JSONObject jp;
                try {
                    jp = projectArray.getJSONObject(i);

                    Intent getIntent = new Intent(
                            INaturalistService.ACTION_GET_PROJECT_DETAILS, null, context, INaturalistService.class);
                    final int projectId = jp.getInt("id");
                    getIntent.putExtra(INaturalistService.PROJECT_ID, projectId);
                    context.startService(getIntent);

                    pendingDetails.add(projectId);
                } catch (JSONException e) {
                    /* failed to parse project id, nothing to do for this project */
                }
            }
        }
    }

    private class ProjectDetailsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Integer projectId = intent.getIntExtra(INaturalistService.PROJECT_ID, -1);

            /* if this project is not pending, nothing to do here */
            if (!pendingDetails.remove(projectId)) {
                return;
            }

            /* all pending projects have been processed, unregister receiver */
            if (pendingDetails.isEmpty()) {
                context.unregisterReceiver(this);
            }

            Log.d(TAG, "Received details for project " + projectId);

            SerializableJSONArray serializableJSONArray =
                    (SerializableJSONArray) intent.getExtras().getSerializable(INaturalistService.PROJECTS_RESULT);
            if (serializableJSONArray == null) {
                return;
            }

            JSONArray projectsJArray = serializableJSONArray.getJSONArray();
            if (projectsJArray == null || projectsJArray.length() < 1) {
                return;
            }

            JSONObject jsonProject;
            try {
                jsonProject = projectsJArray.getJSONObject(0);
            } catch (JSONException ignored) {
                return;
            }
            if (jsonProject == null) {
                return;
            }

            Project project = new Project();
            try {
                project.id = jsonProject.getInt("id");
                project.title = jsonProject.getString("title");
            } catch (JSONException e) {
                /* basic project information could not be parsed, nothing to do with this project */
                return;
            }

            try {
                project.longitude = jsonProject.getDouble("longitude");
                project.latitude = jsonProject.getDouble("latitude");
                project.zoomLevel = (float) jsonProject.getDouble("zoom_level");
            } catch (JSONException ignored) {
                /* non critical data can be omitted from project details */
            }

            if (downloadedProjects == null) {
                Log.e(TAG, "Null downloaded projects configuration");
                return;
            }

            downloadedProjects.add(project);

            /* all pending projects have been processed, broadcast event */
            if (pendingDetails.isEmpty()) {
                Log.d(TAG, "Retrieved details for " + downloadedProjects.size() + " joined projects");
                mIsLoading = false;
                projects = new LinkedHashSet<>(Arrays.asList(sortProjectsArray(downloadedProjects)));
                saveProjectsCache(context);
                context.sendBroadcast(new Intent(ACTION_MY_PROJECTS_LOADED));
            }
        }
    }

    private class UserLoginListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getMyProjects();
        }
    }

    private class UserLogoffListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            deleteProjectsCache(context);
            projects.clear();
            context.sendBroadcast(new Intent(ACTION_MY_PROJECTS_LOADED));
        }
    }

    // endregion

    // region Properties

    private static MyProjectsManager instance;
    @NonNull private final Context context;
    @NonNull private Set<Project> projects;
    @Nullable private Set<Project> downloadedProjects;
    @NonNull private final Set<Integer> pendingDetails;

    // endregion

    // region Lifecycle

    private MyProjectsManager(@NonNull Context context) {
        this.context = context;
        this.pendingDetails = new HashSet<>();
        this.projects = new HashSet<>();

        context.registerReceiver(new UserLoginListener(), new IntentFilter(SignInTask.ACTION_RESULT_SIGNED_IN));

        context.registerReceiver(
                new UserLogoffListener(), new IntentFilter(INaturalistPrefsActivity.ACTION_RESULT_LOGOUT));

        loadProjectsCache(context);

        getMyProjects();
    }

    @NonNull public static MyProjectsManager getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException(
                    "MyProjectsManager.getInstance(Context) have to be called at least once before calling " +
                            "MyProjectsManager.getInstance()");
        }
        return instance;
    }

    @NonNull public static MyProjectsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MyProjectsManager.class) {
                if (instance == null) {
                    instance = new MyProjectsManager(context);
                }
            }
        }
        return instance;
    }

    // endregion

    // region Interface methods

    public boolean isLoading() {
        return mIsLoading;
    }

    @NonNull public Collection<Project> getProjects() {
        return this.projects;
    }

    @Nullable public Project getProject(int id) {
        for (Project p : projects) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }

    // endregion

    // region Utilities

    private Project[] sortProjectsArray(@NonNull Collection<MyProjectsManager.Project> projects) {
        final MyProjectsManager.Project[] toSort = projects.toArray(new MyProjectsManager.Project[projects.size()]);
        Arrays.sort(toSort, new Comparator<MyProjectsManager.Project>() {
            @Override
            public int compare(MyProjectsManager.Project left, MyProjectsManager.Project right) {
                if (left.id == ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject()) {
                    return -1;
                }
                if (right.id == ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject()) {
                    return 1;
                }
                return left.id > right.id ? 1 : -1;
            }
        });
        return toSort;
    }

    private void getMyProjects() {
        Log.d(TAG, "Starting joined projects retrieval");

        SharedPreferences prefs = context.getSharedPreferences("iNaturalistPreferences", Context.MODE_PRIVATE);
        if (prefs.getString("username", null) == null) {
            Log.d(TAG, "User logged out, resetting projects list");
            projects.clear();
            return;
        }

        mIsLoading = true;

        context.registerReceiver(
                new MyProjectsReceiver(), new IntentFilter(INaturalistService.ACTION_JOINED_PROJECTS_RESULT));

        Intent serviceIntent =
                new Intent(INaturalistService.ACTION_GET_JOINED_PROJECTS, null, context, INaturalistService.class);
        context.startService(serviceIntent);

        context.sendBroadcast(new Intent(ACTION_MY_PROJECTS_LOADING_STARTED));
    }

    private void deleteProjectsCache(Context context) {
        context.deleteFile(PROJECTS_CACHE);
    }

    private void saveProjectsCache(Context context) {
        deleteProjectsCache(context);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = context.openFileOutput(PROJECTS_CACHE, Context.MODE_PRIVATE);
            ObjectOutputStream ow;
            ow = new ObjectOutputStream(fileOutputStream);
            ow.writeObject(projects);
            ow.close();
        } catch (IOException e) {
            if (fileOutputStream != null) {
                try { fileOutputStream.close(); } catch (IOException ignored) { }
            }
        }
    }

    private void loadProjectsCache(Context context) {
        FileInputStream inputStream;

        try {
            inputStream = context.openFileInput(PROJECTS_CACHE);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Projects cache file not found");
            return;
        }

        ObjectInputStream is;
        try {
            is = new ObjectInputStream(inputStream);
            projects = (HashSet<Project>) is.readObject();
            is.close();
        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, "Failed to read projects cache", e);
        } finally {
            try { inputStream.close(); }
            catch (IOException ignored) { }
        }
    }

    // endregion
}
