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
            try {
                final JSONObject projectDetails =
                        ConfigurationManager.getInstance().getConfig().getAutoUserJoinProjectDetails();
                if (projectDetails != null) {
                    title = projectDetails.getString("title");
                }
            } catch (JSONException ignored) { }
            setProject(projectId, title);
        }
    }
}
