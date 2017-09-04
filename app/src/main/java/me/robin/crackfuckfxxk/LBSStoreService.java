package me.robin.crackfuckfxxk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;

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

    public boolean signEnable(Context context) {
        if (mockOn()) {
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            Date start = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 0);
            Date end = calendar.getTime();
            boolean enable = now.after(start) && now.before(end);
            if (enable) {
                try {
                    Toast.makeText(context, "现在时间:" + now, Toast.LENGTH_LONG).show();
                } catch (Throwable ignore) {
                }
            }
            return enable;
        }
        return false;
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
