package me.robin.crackfuckfxxk.location.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.LBSStoreService;
import me.robin.crackfuckfxxk.ResponseReadUtils;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import okhttp3.*;

import java.io.IOException;

/**
 * Created by Administrator on 2017-05-12.
 */
public class BDLocationServiceImpl extends BaseLocationServiceImpl {

    @Override
    public void locate(String lat, String lng, final LocationUpdateCallBack locationUpdateCallBack) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme("http").host("api.map.baidu.com").port(80);
        urlBuilder.encodedPath("/geocoder/v2/");
        urlBuilder.addQueryParameter("location", lat + "," + lng);
        urlBuilder.addQueryParameter("coordtype", "wgs84ll");
        urlBuilder.addQueryParameter("output", "json");
        urlBuilder.addQueryParameter("ak", "Tvv44Q86MyrWrrXyZgWjV6wMF4bpYsrw");
        okHttpClient.newCall(new Request.Builder().url(urlBuilder.build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                locationUpdateCallBack.error(BDLocationServiceImpl.this, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String content = ResponseReadUtils.read(response);
                    JSONObject object = JSON.parseObject(content);
                    JSONObject data = new JSONObject();
                    int status = object.getIntValue("status");
                    if (0 != status) {
                        locationUpdateCallBack.error(BDLocationServiceImpl.this, object.getString("message"));
                    } else {
                        object = object.getJSONObject("result");
                        JSONObject loc = object.getJSONObject("location");
                        JSONObject addressComponent = object.getJSONObject("addressComponent");
                        data.put("getLatitude", loc.getDoubleValue("lat"));
                        data.put("getLongitude", loc.getDoubleValue("lng"));
                        data.put("getCountry", addressComponent.getString("country"));
                        data.put("getProvince", addressComponent.getString("province"));
                        data.put("getCity", addressComponent.getString("city"));
                        data.put("getDistrict", addressComponent.getString("district"));
                        data.put("getStreet", addressComponent.getString("street"));
                        data.put("getStreetNumber", addressComponent.getString("street_number"));
                        locationUpdateCallBack.success(BDLocationServiceImpl.this, data);
                    }
                } catch (Exception e) {
                    locationUpdateCallBack.error(BDLocationServiceImpl.this, e.getMessage());
                }
            }
        });
    }
}
