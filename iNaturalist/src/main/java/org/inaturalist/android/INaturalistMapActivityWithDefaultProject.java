package org.inaturalist.android;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.tatzpiteva.golan.ConfigurationManager;

public class INaturalistMapActivityWithDefaultProject extends INaturalistMapActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int projectId = ConfigurationManager.getInstance().getConfig().getAutoUserJoinProject();
        if (projectId > 0) {
            String title = null;
            double latitude = 0;
            double longitude = 0;
            float zoomLevel = 0;
            try {
                final JSONObject projectDetails =
                        ConfigurationManager.getInstance().getConfig().getAutoUserJoinProjectDetails();
                if (projectDetails != null) {
                    title = projectDetails.getString("title");
                    latitude = projectDetails.has("latitude") ? projectDetails.getDouble("latitude") : 0;
                    longitude = projectDetails.has("longitude") ? projectDetails.getDouble("longitude") : 0;
                    zoomLevel = projectDetails.has("zoom_level") ? (float) projectDetails.getDouble("zoom_level") : 0;
                }


            } catch (JSONException ignored) { }
            lockProject(projectId, title, latitude, longitude, zoomLevel);
        }
    }
}
