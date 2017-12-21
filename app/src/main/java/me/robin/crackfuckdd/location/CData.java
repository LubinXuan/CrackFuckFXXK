package me.robin.crackfuckdd.location;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Administrator on 2017-10-26.
 */

public class CData {
    private String value;
    /**
     * 0 String
     * 1 int
     * 2 double
     * 3 float
     */
    private int type;

    public CData() {
    }

    public CData(String value, int type) {
        this.value = value;
        this.type = type;
    }

    public CData(String value) {
        this(value, TypeEnum.STRING);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public Class type() {
        switch (type) {
            case TypeEnum.DOUBLE:
                return double.class;
            case TypeEnum.FLOAT:
                return float.class;
            case TypeEnum.INT:
                return int.class;
            default:
                return String.class;
        }
    }

    public static CData str(String key, JSONObject data) {
        CData cData = new CData();
        cData.setValue(data.getString(key));
        cData.setType(0);
        return cData;
    }

    public static CData _float(String key, JSONObject data) {
        CData cData = new CData();
        cData.setValue(data.getString(key));
        cData.setType(TypeEnum.FLOAT);
        return cData;
    }

    public static CData _double(String key, JSONObject data) {
        CData cData = new CData();
        cData.setValue(data.getString(key));
        cData.setType(TypeEnum.DOUBLE);
        return cData;
    }


    public static CData _int(String key, JSONObject data) {
        CData cData = new CData();
        cData.setValue(data.getString(key));
        cData.setType(TypeEnum.INT);
        return cData;
    }

    public interface TypeEnum {
        int STRING = 0;
        int INT = 1;
        int DOUBLE = 2;
        int FLOAT = 3;
    }
}
