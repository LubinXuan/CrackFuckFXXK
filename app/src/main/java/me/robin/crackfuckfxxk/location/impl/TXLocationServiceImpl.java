package me.robin.crackfuckfxxk.location.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.LBSStoreService;
import me.robin.crackfuckfxxk.ResponseReadUtils;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Administrator on 2017-05-12.
 */
public class TXLocationServiceImpl extends BaseLocationServiceImpl {

    @Override
    public void locate(String lat, String lng, final LocationUpdateCallBack locationUpdateCallBack) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme("http").host("apis.map.qq.com").port(80);
        urlBuilder.encodedPath("/ws/geocoder/v1/");
        urlBuilder.addQueryParameter("location", lat + "," + lng);
        urlBuilder.addQueryParameter("coord_type", "1");
        urlBuilder.addQueryParameter("key", "QPJBZ-FBIHF-RIYJN-JUFYW-JFYUQ-E4FSS");
        okHttpClient.newCall(new Request.Builder().url(urlBuilder.build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                locationUpdateCallBack.error(TXLocationServiceImpl.this, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String content = ResponseReadUtils.read(response);
                    JSONObject object = JSON.parseObject(content);
                    JSONObject data = new JSONObject();
                    int status = object.getIntValue("status");
                    if (0 != status) {
                        locationUpdateCallBack.error(TXLocationServiceImpl.this, object.getString("message"));
                    } else {
                        object = object.getJSONObject("result");
                        JSONObject loc = object.getJSONObject("location");
                        JSONObject addressComponent = object.getJSONObject("address_component");
                        data.put("getLatitude", loc.getDoubleValue("lat"));
                        data.put("getLongitude", loc.getDoubleValue("lng"));
                        data.put("getNation", addressComponent.getString("nation"));
                        data.put("getProvince", addressComponent.getString("province"));
                        data.put("getCity", addressComponent.getString("city"));
                        data.put("getDistrict", addressComponent.getString("district"));
                        data.put("getStreet", addressComponent.getString("street"));
                        data.put("getStreetNumber", addressComponent.getString("street_number"));
                        String address = object.getString("address");
                        JSONObject formatted_addresses = object.getJSONObject("formatted_addresses");
                        if (null != formatted_addresses) {
                            String f_address = formatted_addresses.getString("rough");
                            if (StringUtils.isNotBlank(f_address)) {
                                address = address + f_address;
                            }
                        }
                        data.put("address", address);
                        locationUpdateCallBack.success(TXLocationServiceImpl.this, data);
                    }
                } catch (Exception e) {
                    locationUpdateCallBack.error(TXLocationServiceImpl.this, e.getMessage());
                }
            }
        });
    }
}
