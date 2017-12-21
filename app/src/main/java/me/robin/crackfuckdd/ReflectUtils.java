package me.robin.crackfuckdd;

import android.util.Log;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import java.lang.reflect.Method;

import me.robin.crackfuckdd.location.CData;

/**
 * Created by Administrator on 2017-10-26.
 */

public class ReflectUtils {
    public static void setValue(Object target, String setMethodName, CData cData) {
        try {
            Object value = TypeUtils.cast(cData.getValue(), cData.type(), ParserConfig.getGlobalInstance());
            Method method = target.getClass().getDeclaredMethod(setMethodName, cData.type());
            if (!method.isAccessible()) {
                method.setAccessible(true);
                method.invoke(target, value);
                method.setAccessible(false);
            } else {
                method.invoke(target, value);
            }
        } catch (Exception e) {
            Log.e(DingTalkHook.TAG, "设置失败:" + setMethodName, e);
        }
    }
}
