package org.tatzpiteva.golan;

import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Configuration handling helper methods
 */
public class ConfigHelper {
    // region Constants
    private static final String TAG = "ConfigHelper";
    // endregion

    /** check if the project is in auto-add list in configuration
     *
     * @param projectId ID of the project to test
     *
     * @return `true` if the project is in auto-add list, `false` otherwise
     */
    public static Boolean isProjectAutoAdd(int projectId) {
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects()) {
            if (pj.id == projectId) {
                if (pj.smart_flag == Config.SmartFlag.DEFAULT_READ_ONLY ||
                        pj.smart_flag == Config.SmartFlag.DEFAULT_READ_WRITE) {
                    return true;
                }
            }
        }
        return false;
    }

    /** check if the rpoject is in auto-add list and cannot be deselected by the user
     *
     * @param projectId ID of the project to test
     *
     * @return `true` if the project is in auto project list and its smart flag set to disallow its deselection by the
     *  user
     */
    public static Boolean preventProjectDeselection(int projectId) {
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects ()) {
            if (pj.id == projectId) {
                return pj.smart_flag == Config.SmartFlag.DEFAULT_READ_ONLY;
            }
        }
        return false;
    }

    /** find auto project with specified ID in auto projects collection
     *
     * @param projectId ID of the project to lookup
     *
     * @return project found in configuration or `null` if no such project exists in configuration
     */
    @Nullable
    public static Config.AutoProject findAutoProject(int projectId) {
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects ()) {
            if (pj.id == projectId) {
                return pj;
            }
        }
        return null;
    }

    /**
     * check if all the projects in auto-project collection have their details retrieved from the server
     * @return `true` all the projects details have been retrieved from the server, `false` otherwise
     */
    public static Boolean allAutoProjectHaveDetails() {
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects()) {
            if (!pj.detailsRetrieved) {
                return false;
            }
        }
        return true;
    }

    /** add details to configuration project
     *
     * @param projectId ID of the project from configuration to add details to
     *
     * @param details JSON object as retrieved from the server with project details
     *
     * @return `true` if project details were updated, `false` if the project has not been found in the set of auto
     *      project of if project details could not be parsed
     */
    public static boolean addDetailsToProject(int projectId, JSONObject details) {
        Config.AutoProject ap = findAutoProject(projectId);
        if (ap == null) {
            return false;
        }

        try {
            ap.detailsRetrieved = true;
            ap.title = details.getString("title");
            ap.latitude = details.getDouble("latitude");
            ap.longitude = details.getDouble("longitude");
            ap.zoomLevel = (float) details.getDouble("zoom_level");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse project");
            return false;
        }
        return true;
    }
}
