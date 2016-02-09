package org.tatzpiteva.golan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Launch screen configuration parser.
 * Can be read from
 * http://tatzpiteva.org.il/json/app/about
 */
public class LaunchScreenConfigParser {
    public static LaunchScreenCarouselConfig parseConfig(String jsonBuffer) throws JSONException {
        JSONObject rootObject = new JSONObject(jsonBuffer);

        JSONArray picsArray = rootObject.has("mobile_pics") ?
                rootObject.getJSONArray("mobile_pics") :  rootObject.getJSONArray("pics");

        LaunchScreenCarouselConfig rv = new LaunchScreenCarouselConfig();
        for (int i = 0; i < picsArray.length(); i++) {
            JSONObject picObject = picsArray.getJSONObject(i);

            rv.addPic(new LaunchScreenCarouselConfig.Pic(
                    picObject.getInt("id"),
                    picObject.getString("name"),
                    picObject.getString("url"),
                    picObject.getInt("order")));
        }

        return rv;
    }
}
