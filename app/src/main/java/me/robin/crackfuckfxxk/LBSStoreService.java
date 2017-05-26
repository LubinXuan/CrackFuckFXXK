package me.robin.crackfuckfxxk;

import android.content.SharedPreferences;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import external.org.apache.commons.lang3.StringUtils;

/**
 * Created by Administrator on 2017-05-12.
 */
public class LBSStoreService {

    final private SharedPreferences preferences;

    public LBSStoreService(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void save(String type, JSONObject data) {
        preferences.edit().putString(type, data.toJSONString()).apply();
    }

    public String currentLocation() {
        return preferences.getString("current_location", "");
    }

    public void setCurrentLocation(String location) {
        preferences.edit().putString("current_location", location).apply();
    }

    public JSONObject get(String type) {
        String data = preferences.getString(type, null);
        if (StringUtils.isBlank(data)) {
            Log.w(XposedFXXK.TAG, "没有读取到数据:" + type);
            return null;
        } else {
            return JSON.parseObject(data);
        }
    }

    public boolean mockOn() {
        return preferences.getBoolean("mock-on", false);
    }

    public void mockOn(boolean mockOn) {
        preferences.edit().putBoolean("mock-on", mockOn).apply();
    }

    public void commit() {
        preferences.edit().apply();
    }

}
