package me.robin.crackfuckdd.location;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Administrator on 2017-05-12.
 */
public interface LocationUpdateCallBack {
    void success(LocationService locationService, JSONObject data);
    void error(LocationService locationService, String message);
}
