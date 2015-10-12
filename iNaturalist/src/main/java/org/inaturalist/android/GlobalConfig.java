package org.inaturalist.android;

/**
 * Global configuration parameters
 */
public class GlobalConfig {

    // region Properties

    private static final GlobalConfig mInstance = new GlobalConfig();

    private int[] mAutoJoinProjects = {};

    // endregion

    // region Getters/Setters

    public static GlobalConfig getInstance() {
        return mInstance;
    }

    public int[] getAutoJoinProjects() {
        return mAutoJoinProjects;
    }

    // endregion
}
