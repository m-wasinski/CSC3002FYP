package findndrive.test.functional_tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.example.myapplication.activities.activities.LoginActivity;

/**
 * Created by Michal on 02/03/14.
 */
public class LoginActivityTests extends ActivityInstrumentationTestCase2<LoginActivity> {
    public LoginActivityTests(Class<LoginActivity> activityClass) {
        super(LoginActivity.class);
    }

    @UiThreadTest
    public void testTest()
    {

    }
}
