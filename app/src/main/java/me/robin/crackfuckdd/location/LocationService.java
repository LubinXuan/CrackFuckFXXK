package me.robin.crackfuckdd.location;

/**
 * Created by Administrator on 2017-05-12.
 */
public interface LocationService {
    void locate(String lat, String lng, LocationUpdateCallBack locationUpdateCallBack);
}
