package me.robin.crackfuckdd;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import me.robin.crackfuckdd.location.LocationService;
import me.robin.crackfuckdd.location.LocationUpdateCallBack;
import me.robin.crackfuckdd.location.impl.GDLocationServiceImpl;

public class LBSConfigActivity extends Activity {

    private EditText locEditText;
    private TextView lbsResultTextView;
    private Switch onOrOff;
    private Switch forceOn;
    private Button update;
    private Button saveDataBut;
    private Button updateWorkDayBut;

    private List<LocationService> locationServiceList = new ArrayList<>();

    private Handler handler;

    private LBSStoreService lbsStoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbsconfig);
        this.handler = new Handler(getMainLooper());
        this.lbsStoreService = new LBSStoreService(getSharedPreferences("config", MODE_WORLD_READABLE));
        this.locEditText = (EditText) findViewById(R.id.locEditText);
        this.lbsResultTextView = (TextView) findViewById(R.id.lbsResultTextView);
        this.onOrOff = (Switch) findViewById(R.id.onOrOff);
        this.forceOn = (Switch) findViewById(R.id.forceOn);
        this.update = (Button) findViewById(R.id.update);
        this.saveDataBut = (Button) findViewById(R.id.saveDataBut);
        this.updateWorkDayBut = (Button) findViewById(R.id.updateWorkDayBut);
        this.locationServiceList.add(new GDLocationServiceImpl());
        this.lbsResultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.locEditText.setText(lbsStoreService.currentLocation());

        RadioButton rb = (RadioButton) findViewById(R.id.statusRadio);

        rb.setChecked(lbsStoreService.signEnable(this));

        this.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                update.setEnabled(false);
                final String loc = locEditText.getText().toString();
                Toast.makeText(v.getContext(), "" + loc, Toast.LENGTH_LONG).show();
                try {
                    final String[] sp = loc.split(",");
                    LocationUpdateCallBack locationUpdateCallBack = new LocationUpdateCallBack() {

                        AtomicInteger count = new AtomicInteger(locationServiceList.size());

                        @Override
                        public void success(final LocationService locationService, JSONObject data) {
                            int c = count.decrementAndGet();
                            data.put("updateTime", DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMddHHmm"));
                            lbsStoreService.save(locationService.getClass().getSimpleName(), data);
                            if (c == 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        update.setEnabled(true);
                                    }
                                });
                                lbsStoreService.setCurrentLocation(loc);
                                lbsStoreService.commit();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LBSConfigActivity.this.getApplicationContext(), locationService.getClass().getSimpleName().substring(0, 2) + "更新成功", Toast.LENGTH_LONG).show();
                                    updateLocResult();
                                }
                            });
                        }

                        @Override
                        public void error(final LocationService locationService, final String message) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LBSConfigActivity.this.getApplicationContext(), locationService.getClass().getSimpleName().substring(0, 2) + "更新失败:" + message, Toast.LENGTH_LONG).show();
                                }
                            });
                            final LocationUpdateCallBack callBack = this;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    locationService.locate(sp[0], sp[1], callBack);
                                }
                            }, 5000);
                        }
                    };
                    for (LocationService locationService : locationServiceList) {
                        locationService.locate(sp[0], sp[1], locationUpdateCallBack);
                    }
                } catch (Throwable e) {
                    showMessage(e.getLocalizedMessage());
                }
            }
        });

        this.saveDataBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lbsStoreService.commit();
                Toast.makeText(v.getContext(), "文件保存成功", Toast.LENGTH_LONG).show();
            }
        });

        this.onOrOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lbsStoreService.mockOn(isChecked);
                if (isChecked) {
                    buttonView.setText("启用");
                } else {
                    buttonView.setText("停用");
                }
            }
        });

        this.forceOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lbsStoreService.forceOn(isChecked);
            }
        });

        this.updateWorkDayBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWorkDayBut.setEnabled(false);
                updateWorkDayBut.setText("正在更新");
                lbsStoreService.updateHoliday(new Runnable() {
                    @Override
                    public void run() {
                        lbsStoreService.commit();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateWorkDayBut.setText("更新节日");
                                updateWorkDayBut.setEnabled(true);
                                updateLocResult();
                            }
                        });
                    }
                });
            }
        });

        this.onOrOff.setChecked(lbsStoreService.mockOn());
        this.forceOn.setChecked(lbsStoreService.forceOn());

        this.updateLocResult();
    }


    private void updateLocResult() {
        StringBuilder sb = new StringBuilder();
        JSONObject gdJson = lbsStoreService.get(GDLocationServiceImpl.class.getSimpleName());
        JSONObject specialDays = lbsStoreService.specialDays();
        sb.append("高德(").append(getUpdateTime(gdJson)).append("):").append(getAddress(gdJson)).append("\n");
        sb.append("节日(").append(getString(specialDays, "year")).append(")").append("\n");
        sb.append("节假日:").append(getString(specialDays, "holiday")).append("\n");
        sb.append("特殊工作日:").append(getString(specialDays, "specialWorkday")).append("\n");
        this.lbsResultTextView.setText(sb.toString());
    }

    private String getAddress(JSONObject data) {
        return getString(data, "address");
    }

    private String getUpdateTime(JSONObject data) {
        return getString(data, "updateTime");
    }

    private String getString(JSONObject data, String key) {
        Object value = null == data ? null : data.get(key);
        if (value instanceof JSONArray) {
            return StringUtils.join((JSONArray) value, ",");
        } else if (value instanceof String) {
            return StringUtils.isNotBlank((String) value) ? (String) value : "无";
        } else if (null != value) {
            return TypeUtils.castToString(value);
        } else {
            return "无";
        }
    }

    private void showMessage(String message) {
        Log.i(DingTalkHook.TAG, message);
    }
}
