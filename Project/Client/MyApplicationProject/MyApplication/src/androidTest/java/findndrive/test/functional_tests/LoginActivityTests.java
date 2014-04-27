package findndrive.test.functional_tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.LoginActivity;

/**
 * Created by Michal on 02/03/14.
 */
public class LoginActivityTests extends ActivityInstrumentationTestCase2<LoginActivity> {
    public LoginActivityTests(Class<LoginActivity> activityClass) {
        super(LoginActivity.class);
    }

    private LoginActivity loginActivity;
    private EditText userNameEditText, passwordEditText;
    private Button loginButton;

    public LoginActivityTests()
    {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        loginActivity = getActivity();

        userNameEditText = (EditText) loginActivity.findViewById(R.id.LoginActivityUserNameEditText);
        passwordEditText = (EditText) loginActivity.findViewById(R.id.LoginActivityPasswordTextField);
        loginButton = (Button) loginActivity.findViewById(R.id.LoginActivityLoginUserButton);
    }

    public void testPreConditions() {
        assertTrue(loginActivity != null);
    }

    @UiThreadTest
    public void testLoginValidUser()
    {
        enterDetails();

        loginButton.performClick();

        assertNull(userNameEditText.getError());
        assertNull(passwordEditText.getError());
    }

    @UiThreadTest
    public void testLoginNoUserName()
    {
        enterDetails();

        userNameEditText.setText("");
        loginButton.performClick();
        assertNotNull(userNameEditText.getError());
    }

    @UiThreadTest
    public void testLoginNoPassword()
    {
        enterDetails();

        passwordEditText.setText("");
        loginButton.performClick();
        assertNotNull(passwordEditText.getError());
    }

    private void enterDetails()
    {
        final String username = "john";
        final String password = "password";

        loginActivity.runOnUiThread(new Runnable() {
            public void run() {
                userNameEditText.setText(username);
                passwordEditText.setText(password);
            }
        });

        assertEquals(username, userNameEditText.getText().toString());
        assertEquals(password, passwordEditText.getText().toString());
    }
}
