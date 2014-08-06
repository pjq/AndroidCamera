package me.pjq.camera;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.After;
import org.junit.Before;
import org.robolectric.Robolectric;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.Exception;


@RunWith(RobolectricGradleTestRunner.class)
public class RobolectricMyActivityTest {
    CameraActivity activity;

    @Before
    public void setup() throws Exception {
        activity = Robolectric.buildActivity(CameraActivity.class).create().visible().get();

        assertTrue(null != activity);
    }


    @Test
    public void testShouldHaveApplicationName() throws Exception {
        String name = activity.getResources().getString(R.string.app_name);
        assertThat(name, equalTo("AndroidCamera"));
    }


    @After
    public void tearDown() throws Exception {
        activity.finish();
        activity = null;
        assertTrue(null == activity);
    }
}
