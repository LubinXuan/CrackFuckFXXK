package me.robin.crackfuckfxxk;

import com.alibaba.fastjson.JSONObject;
import me.robin.crackfuckfxxk.location.LocationService;
import me.robin.crackfuckfxxk.location.LocationUpdateCallBack;
import me.robin.crackfuckfxxk.location.impl.GDLocationServiceImpl;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


    //@Test
    public void testGD() throws InterruptedException {
        LocationService gd = new GDLocationServiceImpl();

        final CountDownLatch latch = new CountDownLatch(1);

        gd.locate("30", "120", new LocationUpdateCallBack() {
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