package me.robin.crackfuckdd.location.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import me.robin.crackfuckdd.ResponseReadUtils;
import me.robin.crackfuckdd.location.CData;
import me.robin.crackfuckdd.location.LocationUpdateCallBack;
import okhttp3.*;

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
        params.put("poitype", "商务写字楼");
        params.put("radius", "100");
        params.put("extensions", "all");
        params.put("batch", "false");
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
                        locationUpdateCallBack.error(GDLocationServiceImpl.this, object.toJSONString());
                    } else {
                        JSONObject regeocode = object.getJSONObject("regeocode");
                        JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

                        data.put("setCountry", CData.str("country", addressComponent));
                        data.put("setProvince", CData.str("province", addressComponent));
                        data.put("setCity", CData.str("city", addressComponent));
                        data.put("setDistrict", CData.str("district", addressComponent));
                        data.put("setAdCode", CData.str("adcode", addressComponent));
                        data.put("setCityCode", CData.str("citycode", addressComponent));

                        String address = regeocode.getString("formatted_address");

                        data.put("setAddress", new CData(address));


                        JSONObject street = addressComponent.getJSONObject("streetNumber");

                        String retLoc = street.getString("location");
                        if (StringUtils.isBlank(retLoc)) {
                            data.put("setLatitude", new CData(lat, CData.TypeEnum.DOUBLE));
                            data.put("setLongitude", new CData(lng, CData.TypeEnum.DOUBLE));
                        } else {
                            String[] sp = retLoc.split(",");
                            data.put("setLatitude", new CData(sp[1], CData.TypeEnum.DOUBLE));
                            data.put("setLongitude", new CData(sp[0], CData.TypeEnum.DOUBLE));
                        }

                        data.put("setStreet", CData.str("street", street));
                        data.put("setNumber", CData.str("number", street));


                        //获取道路信息
                        JSONArray roads = regeocode.getJSONArray("roads");
                        if (null != roads && !roads.isEmpty()) {
                            data.put("setRoad", CData.str("name", roads.getJSONObject(0)));
                        } else {
                            data.put("setRoad", CData.str("street", street));
                        }


                        JSONArray aois = regeocode.getJSONArray("aois");
                        if (null != aois && !aois.isEmpty()) {
                            data.put("setAoiName", CData.str("name", aois.getJSONObject(0)));
                        } else {
                            data.put("setAoiName", new CData(null));
                        }


                        JSONArray pois = regeocode.getJSONArray("pois");
                        if (null != pois && !pois.isEmpty()) {
                            String poiName = pois.getJSONObject(0).getString("name");
                            data.put("setPoiName", new CData(poiName));
                            data.put("setDescription", new CData("在" + poiName + "附近"));
                        } else {
                            data.put("setPoiName", new CData(null));
                            data.put("setDescription", new CData(null));
                        }


                        JSONObject wrap = new JSONObject();
                        wrap.put("data", data);
                        wrap.put("address", address);
                        locationUpdateCallBack.success(GDLocationServiceImpl.this, wrap);
                    }
                } catch (Exception e) {
                    locationUpdateCallBack.error(GDLocationServiceImpl.this, e.toString());
                }
            }
        });
    }
}
