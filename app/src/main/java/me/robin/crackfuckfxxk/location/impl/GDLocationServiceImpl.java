package me.robin.crackfuckfxxk.location.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.LBSStoreService;
import me.robin.crackfuckfxxk.ResponseReadUtils;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2017-05-12.
 */
public class GDLocationServiceImpl extends BaseLocationServiceImpl {

    private String key = "edb77d973f25d22db48c48cafc257956";

    @Override
    public void locate(final String lat, final String lng, final LocationUpdateCallBack locationUpdateCallBack) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme("http").host("restapi.amap.com").port(80);
        urlBuilder.encodedPath("/v3/geocode/regeo");
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("location", lng + "," + lat);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        okHttpClient.newCall(new Request.Builder().url(urlBuilder.build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                locationUpdateCallBack.error(GDLocationServiceImpl.this, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String content = ResponseReadUtils.read(response);
                    JSONObject object = JSON.parseObject(content);
                    JSONObject data = new JSONObject();
                    int status = object.getIntValue("status");
                    if (1 != status) {
                        locationUpdateCallBack.error(GDLocationServiceImpl.this, object.getString("info"));
                    } else {
                        JSONObject regeocode = object.getJSONObject("regeocode");
                        JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

                        data.put("getCountry", addressComponent.getString("country"));
                        data.put("getProvince", addressComponent.getString("province"));
                        data.put("getCity", addressComponent.getString("city"));
                        data.put("getDistrict", addressComponent.getString("district"));

                        JSONObject street = addressComponent.getJSONObject("streetNumber");

                        String retLoc = street.getString("location");
                        if (StringUtils.isBlank(retLoc)) {
                            data.put("getLatitude", Double.parseDouble(lat));
                            data.put("getLongitude", Double.parseDouble(lng));
                        } else {
                            String[] sp = retLoc.split(",");
                            data.put("getLatitude", Double.parseDouble(sp[1]));
                            data.put("getLongitude", Double.parseDouble(sp[0]));
                        }

                        data.put("getStreet", street.getString("street"));
                        data.put("getStreetNum", street.getString("number"));

                        JSONArray aois = regeocode.getJSONArray("aois");
                        if (null != aois && !aois.isEmpty()) {
                            data.put("getAoiName", aois.getJSONObject(0).getString("name"));
                        }
                        locationUpdateCallBack.success(GDLocationServiceImpl.this, data);
                    }
                } catch (Exception e) {
                    locationUpdateCallBack.error(GDLocationServiceImpl.this, e.getMessage());
                }
            }
        });
    }
}
