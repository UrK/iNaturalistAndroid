package org.tatzpiteva.golan;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser of JSON configuration file
 */
public class ConfigParser {
    private static final String TAG = "ConfigParser";

    public static Config parseConfig(String jsonBuffer) throws IOException, JSONException {
        JSONArray jarr = new JSONArray(jsonBuffer);

        List<Config.AutoProject> autoProjects = new ArrayList<>(jarr.length());

        for (int i = 0; i < jarr.length(); i++) {
            Config.AutoProject ap = parseProject(jarr.getJSONObject(i));
            if (ap != null) {
                autoProjects.add(ap);
            }
        }

        return new Config(autoProjects);
    }

    private static Config.AutoProject parseProject(JSONObject jobj) {
        if (jobj == null) {
            return null;
        }

        Config.AutoProject rv = new Config.AutoProject();

        try {
            String strId = jobj.getString("id");
            if (strId != null) {
                rv.id = Integer.valueOf(strId);
            }

            rv.title = jobj.getString("title");

            String strSmartFlag = jobj.getString("smart_flag");
            if (strSmartFlag != null) {
                rv.smart_flag = Config.SmartFlag.fromInt(Integer.valueOf(strSmartFlag));
            }

            String strMenuFlag = jobj.getString("menu_flag");
            if (strMenuFlag != null) {
                rv.menu_flag = Config.SmartFlag.fromInt(Integer.valueOf(strMenuFlag));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid auto project JSON: " + jobj.toString());
            return null;
        }

        return rv;
    }
}
