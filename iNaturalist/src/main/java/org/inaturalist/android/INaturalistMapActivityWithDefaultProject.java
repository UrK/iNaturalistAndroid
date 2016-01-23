package org.inaturalist.android;

import android.os.Bundle;

import org.tatzpiteva.golan.MyProjectsManager;

public class INaturalistMapActivityWithDefaultProject extends INaturalistMapActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int projectId = getIntent().getIntExtra(INTENT_PARAM_PROJECT_ID, -1);
        MyProjectsManager.Project project = MyProjectsManager.getInstance().getProject(projectId);

        if (project != null) {
            lockProject(project.id, project.title, project.latitude, project.longitude, project.zoomLevel);
        }
    }
}
