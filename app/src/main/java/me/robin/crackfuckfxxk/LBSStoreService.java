package me.robin.crackfuckfxxk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.location.impl.BaseLocationServiceImpl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017-05-12.
 */
public class LBSStoreService {

    private static final String holidayApi = "http://tool.bitefu.net/jiari/data/{year}.txt";
    private static final String specialWorkDayApi = "http://tool.bitefu.net/jiari/data/{year}_w.txt";

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

        if (forceOn()) {
            return true;
        }

        if (mockOn()) {

            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();

            JSONObject specialDays = specialDays();

            String date = DateFormatUtils.format(now, "yyyyMMdd");

            int day = calendar.get(Calendar.DAY_OF_WEEK);

            if (day > 1 & day < 7) {
                JSONArray holiday = specialDays.getJSONArray("holiday");
                if (null != holiday && holiday.contains(date)) {
                    toast(context, "今天是" + date + ",是节假日");
                    return false;
                }
            } else {
                JSONArray specialWorkday = specialDays.getJSONArray("specialWorkday");
                if (null != specialWorkday && !specialWorkday.contains(date)) {
                    toast(context, "今天是" + date + "，不是工作日");
                    return false;
                }
            }

            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            Date start = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 0);
            Date end = calendar.getTime();
            boolean enable = now.after(start) && now.before(end);
            if (!enable) {
                toast(context, "现在时间:" + DateFormatUtils.format(now, "yyyyMMdd HH:mm"));
            }
            return enable;
        }
        return false;
    }

    private void toast(Context context, String content) {
        try {
            Toast.makeText(context, content, Toast.LENGTH_LONG).show();
        } catch (Throwable ignore) {
        }
    }

    public boolean mockOn() {
        return preferences.getBoolean("mock-on", false);
    }

    public boolean forceOn() {
        return preferences.getBoolean("force-on", false);
    }

    public void forceOn(boolean forceOn) {
        preferences.edit().putBoolean("force-on", forceOn).apply();
    }

    public void mockOn(boolean mockOn) {
        preferences.edit().putBoolean("mock-on", mockOn).apply();
    }

    public void commit() {
        preferences.edit().apply();
    }

    public JSONObject specialDays() {
        return get("specialDays");
    }

    public void updateHoliday(final Runnable runnable) {
        final String year = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyy");
        Callback callback = new Callback() {

            AtomicInteger count = new AtomicInteger(2);

            Map<String, Object> dataMap = new HashMap<>();

            {
                dataMap.put("year", year);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                updateSpecialDays(count, dataMap, runnable);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String content = ResponseReadUtils.read(response);
                    String[] days = StringUtils.split(content, "\n");
                    List<String> date = new ArrayList<>();
                    for (String day : days) {
                        if (StringUtils.isNotBlank(day)) {
                            date.add(year+StringUtils.trim(day));
                        }
                    }
                    if (date.isEmpty()) {
                        return;
                    }
                    if (call.request().url().encodedPath().endsWith(year + ".txt")) {
                        dataMap.put("holiday", date);
                    } else {
                        dataMap.put("specialWorkday", date);
                    }
                } finally {
                    updateSpecialDays(count, dataMap, runnable);
                }
            }
        };
        Request request = new Request.Builder().url(holidayApi.replace("{year}", year)).build();
        BaseLocationServiceImpl.okHttpClient.newCall(request).enqueue(callback);
        request = new Request.Builder().url(specialWorkDayApi.replace("{year}", year)).build();
        BaseLocationServiceImpl.okHttpClient.newCall(request).enqueue(callback);
    }

    private void updateSpecialDays(AtomicInteger count, Map<String, Object> dataMap, Runnable runnable) {
        int c = count.decrementAndGet();
        if (c == 0) {
            this.preferences.edit().putString("specialDays", JSON.toJSONString(dataMap)).apply();
            runnable.run();
        }
    }

}
