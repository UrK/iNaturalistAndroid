package org.tatzpiteva.golan;

import java.util.Collection;

/**
 * Configuration handling helper methods
 */
public class ConfigHelper {
    /** check if the project is in auto-add list in configuration
     *
     * @param projectId ID of the project to test
     *
     * @return `true` if the project is in auto-add list, `false` otherwise
     */
    public static Boolean isProjectAutoAdd(int projectId) {
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects()) {
            if (pj.id == projectId) {
                return true;
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
        for (Config.AutoProject pj : ConfigurationManager.getInstance().getConfig().getAutoProjects()) {
            if (pj.id == projectId) {
                return pj.smart_flag == Config.SmartFlag.DEFAULT_READ_ONLY;
            }
        }
        return false;
    }
}
