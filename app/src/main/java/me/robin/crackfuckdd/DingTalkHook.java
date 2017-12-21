package me.robin.crackfuckdd;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.robin.crackfuckdd.location.CData;
import me.robin.crackfuckdd.location.impl.GDLocationServiceImpl;

/**
 * Created by Administrator on 2017-05-12.
 * 处理定位数据
 */
public class DingTalkHook implements IXposedHookLoadPackage {

    static final String TAG = "FUCKDD";
    static final String TAG_TRACE = "FUCKDD_TRACE";

    private Method replaceHookedMethod = reflectMethod(XC_MethodReplacement.class, "replaceHookedMethod");
    private Method beforeHookedMethod = reflectMethod(XC_MethodHook.class, "beforeHookedMethod");
    private Method afterHookedMethod = reflectMethod(XC_MethodHook.class, "afterHookedMethod");

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        if (StringUtils.equals(lpp.packageName, "com.alibaba.android.rimet")) {
            Log.i(TAG, "开始Hook定位组件:" + lpp.packageName + " process:" + lpp.processName);
            hook_method("com.alibaba.lightapp.runtime.activity.CommonWebViewActivity", lpp.classLoader, "onCreate"
                    , Bundle.class
                    , new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            super.beforeHookedMethod(methodHookParam);
                            try {
                                Bundle bundle = (Bundle) methodHookParam.args[0];
                                if (null == bundle) {
                                    bundle = ((Activity) methodHookParam.thisObject).getIntent().getExtras();
                                }
                                for (String key : bundle.keySet()) {
                                    Log.i(TAG, "key:" + key + "   value:" + bundle.get(key));
                                }
                            } catch (Throwable e) {
                                Log.e(TAG, "参数异常", e);
                            }
                        }
                    });
            this.disableWifiPhoneManager(lpp);

            this.mockGps(lpp.classLoader);

            final XC_MethodHook gdLocation = new XC_MethodHook() {
                LBSStoreService lbsStoreService = new LBSStoreService(new XSharedPreferences("me.robin.crackfuckdd", "config"));

                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (!lbsStoreService.signEnable(AndroidAppHelper.currentApplication().getApplicationContext())) {
                        return;
                    }
                    final JSONObject locationData = lbsStoreService.get(GDLocationServiceImpl.class.getSimpleName());
                    if (null != locationData) {
                        Log.i(TAG, "修改前:" + methodHookParam.args[0]);
                        Object param = methodHookParam.args[0];
                        JSONObject dataMap = locationData.getJSONObject("data");
                        for (String key : dataMap.keySet()) {
                            try {
                                CData cData = dataMap.getObject(key, CData.class);
                                ReflectUtils.setValue(param, key, cData);
                            } catch (Throwable e) {
                                Log.e(TAG, "处理异常 " + key + "  " + dataMap.getString(key));
                            }
                        }
                        Log.i(TAG, "修改后:" + methodHookParam.args[0]);
                    }
                }
            };

            //hook所有调用高德定位的LocationListener
            hook_method("com.amap.api.location.AMapLocationClient", lpp.classLoader, "setLocationListener", "com.amap.api.location.AMapLocationListener", new XC_MethodHook() {
                final Set<Class> hookListener = new HashSet<>();

                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Object listener = methodHookParam.args[0];
                    if (hookListener.add(listener.getClass())) {
                        hookMethod(listener.getClass(), "onLocationChanged", "com.amap.api.location.AMapLocation", gdLocation);
                    }
                }
            });
        }
    }

    private void mockGps(ClassLoader classLoader) {
        hook_method("android.location.LocationManager", classLoader, "getGpsStatus",
                GpsStatus.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        //printCallStack();


                        GpsStatus gss = (GpsStatus) param.getResult();
                        if (gss == null)
                            return;
                        try {
                            Method m = GpsStatus.class.getDeclaredMethod("setStatus", int.class, int[].class, float[].class, float[].class, float[].class, int.class, int.class, int.class);
                            m.setAccessible(true);
                            int svCount = 5;
                            int[] prns = {1, 2, 3, 4, 5};
                            float[] snrs = {0, 0, 0, 0, 0};
                            float[] elevations = {0, 0, 0, 0, 0};
                            float[] azimuths = {0, 0, 0, 0, 0};
                            int ephemerisMask = 0x1f;
                            int almanacMask = 0x1f;
                            int usedInFixMask = 0x1f;
                            m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                            param.setResult(gss);
                        } catch (Exception e) {
                            Log.e(TAG, "GPS状态设置失败", e);
                        }
                    }
                }
        );

        hook_method("android.location.LocationManager", classLoader, "requestLocationUpdates",
                String.class,
                long.class,
                float.class,
                LocationListener.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        //printCallStack();
                        LocationListener ll = (LocationListener) param.args[3];
                        try {
                            Method m = LocationListener.class.getMethod("onLocationChanged", Location.class);
                            Location l = new Location(LocationManager.GPS_PROVIDER);
                            double la = 39.862559;//帝都的经纬度
                            double lo = 116.449535;
                            m.invoke(ll, l);
                            Log.i(TAG, "fake location: " + la + ", " + lo);
                        } catch (Throwable e) {
                            Log.e(TAG, "GPS设置失败", e);
                        }
                        return null;
                    }
                }
        );
    }

    private void disableWifiPhoneManager(XC_LoadPackage.LoadPackageParam param) {
        hook_method("android.telephony.TelephonyManager", param.classLoader, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.i(TAG, "禁用TelephonyManager.getCellLocation");
                //printCallStack();
                methodHookParam.setResult(null);
            }
        });

        hook_method("android.telephony.TelephonyManager", param.classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.i(TAG, "禁用TelephonyManager.getNeighboringCellInfo");
                //printCallStack();
                methodHookParam.setResult(null);
            }
        });

        hook_method("android.net.wifi.WifiManager", param.classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.i(TAG, "禁用WifiManager.getScanResults");
                //printCallStack();
                methodHookParam.setResult(Collections.emptyList());
            }
        });

    }


    private void hook_method(final String className, ClassLoader classLoader, final String method, Object... parameterTypes) {
        Class targetClass = XposedHelpers.findClassIfExists(className, classLoader);
        if (null == targetClass) {
            fileLog("无法找到类[" + className + "]无法完成对方法[" + method + "]的Hook");
            return;
        }
        hookMethod(targetClass, method, parameterTypes);
    }

    private void hookMethod(final Class clazz, final String method, Object... parameterTypes) {
        try {

            int methodHookIdx = parameterTypes.length - 1;

            final XC_MethodHook methodHook = (XC_MethodHook) parameterTypes[methodHookIdx];

            if (methodHook instanceof XC_MethodReplacement) {
                parameterTypes[methodHookIdx] = new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            return replaceHookedMethod.invoke(methodHook, methodHookParam);
                        } catch (Throwable e) {
                            Log.e(TAG, "执行" + clazz.getName() + "." + method + "失败", e);
                            throw e;
                        }
                    }
                };
            } else {
                parameterTypes[methodHookIdx] = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            afterHookedMethod.invoke(methodHook, methodHookParam);
                        } catch (Throwable e) {
                            Log.e(TAG, "执行" + clazz.getName() + "." + method + "失败", e);
                            throw e;
                        }
                    }

                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            beforeHookedMethod.invoke(methodHook, methodHookParam);
                        } catch (Throwable e) {
                            Log.e(TAG, "执行" + clazz.getName() + "." + method + "失败", e);
                            throw e;
                        }
                    }
                };
            }
            XposedHelpers.findAndHookMethod(clazz, method, parameterTypes);
            Log.i(TAG, "对类[" + clazz.getName() + "]的方法[" + method + "]Hook成功");
        } catch (Throwable e) {
            fileLog("对类[" + clazz.getName() + "]的方法[" + method + "]Hook操作失败  E:" + e.toString());
        }
    }

    private void printCallStack() {
        Log.i(TAG_TRACE, "######################################################################################################");
        Log.i(TAG_TRACE, "######################################################################################################");
        Log.i(TAG_TRACE, "######################################################################################################");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 2; i < elements.length; i++) {
            StackTraceElement e = elements[i];
            Log.i(TAG_TRACE, e.getClassName() + "." + e.getMethodName() + "   line:" + e.getLineNumber());
        }
    }

    private Method reflectMethod(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, XC_MethodHook.MethodHookParam.class);
            method.setAccessible(true);
            return method;
        } catch (Throwable e) {
            return null;
        }
    }

    private void fileLog(String message) {
        Log.w(TAG, message);
        XposedBridge.log(message);
    }

}
