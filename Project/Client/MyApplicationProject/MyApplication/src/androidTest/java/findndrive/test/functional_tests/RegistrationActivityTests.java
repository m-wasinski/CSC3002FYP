package findndrive.test.functional_tests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.R;


public class RegistrationActivityTests extends ActivityInstrumentationTestCase2<com.example.myapplication.activities.activities.RegistrationActivity>
{
    private com.example.myapplication.activities.activities.RegistrationActivity registrationActivity;
    private EditText userNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmedPasswordEditText;
    private Button registerButton;

    public RegistrationActivityTests()
    {
        super(com.example.myapplication.activities.activities.RegistrationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        registrationActivity = getActivity();

        userNameEditText = (EditText) registrationActivity.findViewById(R.id.UserNameTextField);
        emailAddressEditText = (EditText) registrationActivity.findViewById(R.id.EmailTextField);
        passwordEditText = (EditText) registrationActivity.findViewById(R.id.RegistrationPasswordTextField);
        confirmedPasswordEditText = (EditText) registrationActivity.findViewById(R.id.RegistrationConfirmPasswordTextField);
        registerButton = (Button) registrationActivity.findViewById(R.id.RegisterNewUserButton);
    }

    public void testPreConditions() {
        assertTrue(registrationActivity != null);
    }

    public void testRegisterValidUser()
    {
        enterDetails();

        registerButton.callOnClick();
        assertNull(userNameEditText.getError());
        assertNull(emailAddressEditText.getError());
        assertNull(passwordEditText.getError());
        assertNull(confirmedPasswordEditText.getError());
    }


    public void testRegisterUserWithDifferentPasswords()
    {
        enterDetails();

        confirmedPasswordEditText.setText("password2");
        assertEquals("password2", confirmedPasswordEditText.getText().toString());

        registerButton.performClick();

        assertNotNull(passwordEditText.getError());
        assertEquals("Both passwords must match.", passwordEditText.getError());
    }

    private void enterDetails()
    {
        final String username = "John";
        final String email = "john@gmail.com";
        final String password = "password";
        final String confirmedPassword = "password";

        registrationActivity.runOnUiThread(new Runnable() {
            public void run() {
                userNameEditText.setText(username);
                emailAddressEditText.setText(email);
                passwordEditText.setText(password);
                confirmedPasswordEditText.setText(confirmedPassword);
            }
        });

        getInstrumentation().waitForIdleSync();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(username, userNameEditText.getText().toString());
        assertEquals(email, emailAddressEditText.getText().toString());
        assertEquals(password, passwordEditText.getText().toString());
        assertEquals(confirmedPassword, confirmedPasswordEditText.getText().toString());
    }

}
