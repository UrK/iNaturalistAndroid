package org.inaturalist.android;

import android.content.Intent;
import android.os.Bundle;

import org.tatzpiteva.golan.MyProjectsManager;

public class INaturalistMapActivityWithDefaultProject extends INaturalistMapActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processLockIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processLockIntent(intent);
    }

    private void processLockIntent(Intent intent) {
        int projectId = intent.getIntExtra(INTENT_PARAM_PROJECT_ID, -1);
        MyProjectsManager.Project project = MyProjectsManager.getInstance().getProject(projectId);

        if (project != null) {
            lockProject(project.id, project.title, project.latitude, project.longitude, project.zoomLevel);
        }
    }
}
