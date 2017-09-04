package me.robin.crackfuckfxxk;

import android.app.AndroidAppHelper;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.robin.crackfuckfxxk.location.impl.BDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.GDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.TXLocationServiceImpl;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017-05-12.
 * 处理定位数据
 */
public class XposedFXXK implements IXposedHookLoadPackage {

    static final String TAG = "XposedFXXK";

    private LBSStoreService lbsStoreService;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (null == lbsStoreService) {
            lbsStoreService = new LBSStoreService(new XSharedPreferences("me.robin.crackfuckfxxk", "config"));
        }

        if (StringUtils.contains(loadPackageParam.packageName, "com.facishare.fs")) {

            Log.e(TAG, "开始Hook定位组件:" + loadPackageParam.packageName);
            hookBD(loadPackageParam);
            hookGD(loadPackageParam);
            hookTX(loadPackageParam);
        }
    }

    private void hookBD(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.fxiaoke.location.impl.BdLocation", lpparam.classLoader, "onReceiveLocation", "com.baidu.location.BDLocation", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                if (!lbsStoreService.signEnable(AndroidAppHelper.currentApplication().getApplicationContext())) {
                    return;
                }

                final JSONObject locationData = lbsStoreService.get(BDLocationServiceImpl.class.getSimpleName());
                if (null == locationData) {
                    return;
                }
                setValue(methodHookParam.args[0], locationData, "getLatitude", double.class);
                setValue(methodHookParam.args[0], locationData, "getLongitude", double.class);
                setValue(methodHookParam.args[0], locationData, "getBuildingName", String.class);


                Field mAddrField = methodHookParam.args[0].getClass().getDeclaredField("mAddr");
                mAddrField.setAccessible(true);
                Object mAddr = mAddrField.get(methodHookParam.args[0]);
                setFieldValue("country", mAddr, "getCountry", locationData);
                setFieldValue("province", mAddr, "getProvince", locationData);
                setFieldValue("district", mAddr, "getDistrict", locationData);
                setFieldValue("street", mAddr, "getStreet", locationData);
                setFieldValue("streetNumber", mAddr, "getStreetNumber", locationData);
                setFieldValue("city", mAddr, "getCity", locationData);
                mAddrField.setAccessible(false);

            }
        });
    }

    private void setFieldValue(String fieldName, Object mAddr, String dataName, JSONObject locationData) {
        try {
            String value = locationData.getString(dataName);
            Field field = mAddr.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(mAddr, value);
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e(XposedFXXK.TAG, "字段：" + fieldName + " 设置失败", e);
        }
    }

    private void hookGD(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.fxiaoke.location.impl.GdLocation", lpparam.classLoader, "onLocationChanged", "com.amap.api.location.AMapLocation", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                if (!lbsStoreService.signEnable(AndroidAppHelper.currentApplication().getApplicationContext())) {
                    return;
                }

                final JSONObject locationData = lbsStoreService.get(GDLocationServiceImpl.class.getSimpleName());
                if (null == locationData) {
                    return;
                }
                setValue(methodHookParam.args[0], locationData, "getLatitude", double.class);
                setValue(methodHookParam.args[0], locationData, "getLongitude", double.class);
                setValue(methodHookParam.args[0], locationData, "getCountry", String.class);
                setValue(methodHookParam.args[0], locationData, "getProvince", String.class);
                setValue(methodHookParam.args[0], locationData, "getCity", String.class);
                setValue(methodHookParam.args[0], locationData, "getDistrict", String.class);
                setValue(methodHookParam.args[0], locationData, "getStreet", String.class);
                setValue(methodHookParam.args[0], locationData, "getStreetNum", "setNumber", String.class);
                setValue(methodHookParam.args[0], locationData, "getAoiName", String.class);
            }
        });
    }

    private void hookTX(final XC_LoadPackage.LoadPackageParam lpparam) {
        hookMethodResult(lpparam.classLoader, "getLatitude", double.class);
        hookMethodResult(lpparam.classLoader, "getLongitude", double.class);
        hookMethodResult(lpparam.classLoader, "getNation", String.class);
        hookMethodResult(lpparam.classLoader, "getProvince", String.class);
        hookMethodResult(lpparam.classLoader, "getCity", String.class);
        hookMethodResult(lpparam.classLoader, "getDistrict", String.class);
        hookMethodResult(lpparam.classLoader, "getStreet", String.class);
        hookMethodResult(lpparam.classLoader, "getStreetNo", String.class);
    }

    private void hookMethodResult(ClassLoader classLoader, String methodName, final Class resultClazz) {
        XposedHelpers.findAndHookMethod("ct.cz", classLoader, methodName, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                if (!lbsStoreService.signEnable(AndroidAppHelper.currentApplication().getApplicationContext())) {
                    return;
                }

                JSONObject locationData = lbsStoreService.get(TXLocationServiceImpl.class.getSimpleName());
                if (null == locationData) {
                    return;
                }
                if (resultClazz.equals(double.class)) {
                    methodHookParam.setResult(locationData.getDoubleValue(methodHookParam.method.getName()));
                } else if (resultClazz.equals(String.class)) {
                    methodHookParam.setResult(locationData.getString(methodHookParam.method.getName()));
                }
            }
        });
    }


    private void setValue(Object target, JSONObject locationDate, String methodName, Class<?> paramClazz) {
        setValue(target, locationDate, methodName, methodName, paramClazz);
    }

    private void setValue(Object target, JSONObject locationDate, String fieldName, String setMethodName, Class<?> paramClazz) {
        Object value = null;
        if (paramClazz.equals(double.class)) {
            value = locationDate.getDoubleValue(fieldName);
        } else if (paramClazz.equals(String.class)) {
            value = locationDate.getString(fieldName);
        }

        setMethodName = setMethodName.replace("get", "set");

        try {
            Method method = target.getClass().getDeclaredMethod(setMethodName, paramClazz);
            if (!method.isAccessible()) {
                method.setAccessible(true);
                method.invoke(target, value);
                method.setAccessible(false);
            } else {
                method.invoke(target, value);
            }
        } catch (Exception e) {
            Log.e(TAG, "设置失败:" + setMethodName, e);
        }
    }
}
