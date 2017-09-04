package me.robin.crackfuckfxxk;

import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.location.LocationService;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import me.robin.crackfuckfxxk.location.impl.BDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.GDLocationServiceImpl;
import me.robin.crackfuckfxxk.location.impl.TXLocationServiceImpl;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    public static void main(String[] args) throws InterruptedException{
        //locate(new GDLocationServiceImpl());
        //locate(new BDLocationServiceImpl());
        locate(new TXLocationServiceImpl());
    }

    private static void locate(LocationService locationService) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        locationService.locate("30.2727826988", "120.1242878758", new LocationUpdateCallBack() {
            @Override
            public void success(LocationService locationService, JSONObject data) {
                System.out.println(data);
                latch.countDown();
            }

            @Override
            public void error(LocationService locationService, String message) {
                System.out.println(message);
                latch.countDown();
            }
        });
        latch.await();
    }
}