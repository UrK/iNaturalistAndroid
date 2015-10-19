package org.inaturalist.android;

/**
 * Global configuration parameters
 */
public class GlobalConfig {

    // region Properties

    private static final GlobalConfig mInstance = new GlobalConfig();

    // Golan Wildlife project- 4527
    private int mAutoJoinProject = 4527;

    // endregion

    // region Getters/Setters

    public static GlobalConfig getInstance() {
        return mInstance;
    }

    public int getAutoJoinProject() {
        return mAutoJoinProject;
    }

    // endregion
}
