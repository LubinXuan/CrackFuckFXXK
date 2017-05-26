package me.robin.crackfuckfxxk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.location.LocationService;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import me.robin.crackfuckfxxk.location.impl.BDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.GDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.TXLocationServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LBSConfigActivity extends Activity {

    private EditText locEditText;
    private EditText lbsResultTextView;
    private Switch onOrOff;
    private Button update;
    private Button saveDataBut;

    private List<LocationService> locationServiceList = new ArrayList<>();

    private Handler handler;

    private LBSStoreService lbsStoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbsconfig);
        this.handler = new Handler();
        this.lbsStoreService = new LBSStoreService(getSharedPreferences( "config", MODE_WORLD_READABLE));
        this.locEditText = (EditText) findViewById(R.id.locEditText);
        this.lbsResultTextView = (EditText) findViewById(R.id.lbsResultTextView);
        this.onOrOff = (Switch) findViewById(R.id.onOrOff);
        this.update = (Button) findViewById(R.id.update);
        this.saveDataBut = (Button) findViewById(R.id.saveDataBut);
        this.locationServiceList.add(new BDLocationServiceImpl());
        this.locationServiceList.add(new TXLocationServiceImpl());
        this.locationServiceList.add(new GDLocationServiceImpl());

        this.locEditText.setText(lbsStoreService.currentLocation());

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
                        public void success(LocationService locationService, JSONObject data) {
                            int c = count.decrementAndGet();
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
                            showMessage(data.toJSONString());
                        }

                        @Override
                        public void error(LocationService locationService, String message) {
                            showMessage(locationService.getClass().getSimpleName() + "  " + message);
                            locationService.locate(sp[0], sp[1], this);
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

        this.onOrOff.setChecked(lbsStoreService.mockOn());
    }

    private void showMessage(String message) {
        Log.i(XposedFXXK.TAG, message);
    }
}
