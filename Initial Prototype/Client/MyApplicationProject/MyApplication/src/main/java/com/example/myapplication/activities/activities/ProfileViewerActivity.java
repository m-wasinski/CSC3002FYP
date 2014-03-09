package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.GenderTypes;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.UserRetrieverDTO;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.factories.ServiceTaskFactory;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.Gson;

/**
 * This activity is used to display profile information of the user whose id is passed in the bundle.
 * User's information is retrieved from the server for privacy considerations.
 */
public class ProfileViewerActivity extends BaseActivity implements WCFImageRetrieved, WCFServiceCallback<User,Void> {

    protected int userId;

    protected TextView memberNameTextView;
    protected TextView memberEmailAddressTextView;
    protected TextView memberGenderTextView;
    protected TextView memberSinceTextView;
    protected TextView dateOfBirthTextVIew;
    protected TextView phoneNumberTextView;
    protected TextView lastLogonTextView;
    protected TextView ratingTextView;
    protected TextView journeysTextView;
    protected int mode;
    protected ImageView profileImageView;

    private ProgressBar profileImageProgressBar;
    protected ProgressBar profileProgressBar;

    private WcfPostServiceTask<UserRetrieverDTO> detailsRetriever;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * The onResume method checks what mode this activity has been launched in.
     * If the mode is set to viewing, it means we are viewing another users' profile and the information about them should be retrieved from the web service.
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        getProfilePicture();
        if(mode == IntentConstants.PROFILE_VIEWER_VIEWING)
        {
            profileProgressBar.setVisibility(View.VISIBLE);
            getPersonalDetails();
        }

    }

    private void getPersonalDetails()
    {
        detailsRetriever = ServiceTaskFactory.getPersonDetails(this, appManager.getAuthorisationHeaders(), new UserRetrieverDTO(appManager.getUser().getUserId(), userId), this);
        detailsRetriever.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(detailsRetriever != null)
        {
            detailsRetriever.cancel(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_viewer);

        Bundle bundle = getIntent().getExtras();

        // Initialise local variables.
        userId = bundle.getInt(IntentConstants.USER, 0);
        mode = bundle.getInt(IntentConstants.PROFILE_VIEWER_MODE);

        //Initialise UI elements.
        memberNameTextView = (TextView) findViewById(R.id.ProfileViewerActivityNameTextView);
        memberEmailAddressTextView = (TextView) findViewById(R.id.ProfileViewerActivityEmailTextView);
        memberGenderTextView = (TextView) findViewById(R.id.ProfileViewerActivityGenderTextView);
        memberSinceTextView = (TextView) findViewById(R.id.ProfileViewerActivityMemberSinceTextView);
        dateOfBirthTextVIew = (TextView) findViewById(R.id.ProfileViewerActivityDateOfBirthTextView);
        phoneNumberTextView = (TextView) findViewById(R.id.ProfileViewerActivityPhoneNumberTextView);
        lastLogonTextView = (TextView) findViewById(R.id.ProfileViewerActivityLastLogonTextView);
        ratingTextView = (TextView) findViewById(R.id.ProfileViewerActivityRatingTextView);
        profileImageView = (ImageView) findViewById(R.id.ProfileViewerActivityProfileIconImageView);
        profileImageProgressBar = (ProgressBar) findViewById(R.id.ProfileViewerActivityProfileImageProgressBar);
        journeysTextView = (TextView) findViewById(R.id.ProfileViewerActivityJourneysTextView);
        profileProgressBar = (ProgressBar) findViewById(R.id.ProfileViewerActivityProgressBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Profile viewer", getResources().getString(R.string.ProfileViewerHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * After user's information has been successfully retrieved from the web service, it's time to display it in the activity.
     *
     * @param user - user object retrieved from the web service.
     */
    protected void fillPersonDetails(final User user)
    {
        setTitle(user.getUserName());
        memberNameTextView.setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getUserName()+")");
        memberEmailAddressTextView.setText(user.getEmailAddress() == null ? "Private" : user.getEmailAddress());
        memberGenderTextView.setText(user.getGender() == GenderTypes.PRIVATE ? "Private" : Utilities.translateGender(user.getGender()));
        memberSinceTextView.setText(DateTimeHelper.getSimpleDate(user.getMemberSince()));
        dateOfBirthTextVIew.setText(user.getDateOfBirth() == null ? "Private" : DateTimeHelper.getSimpleDate(user.getDateOfBirth()));
        phoneNumberTextView.setText(user.getPhoneNumber() == null ? "Private" : user.getPhoneNumber());
        lastLogonTextView.setText(DateTimeHelper.getSimpleDate(user.getLastLogon()) + " " + DateTimeHelper.getSimpleTime(user.getLastLogon()));
        ratingTextView.setText(String.valueOf(user.getAverageRating() == -1 ? "Private" :  String.valueOf(user.getAverageRating())));
        journeysTextView.setText(user.getJourneysVisible() ? ("View " + user.getUserName() + "'s journeys.") : "Private");

        // If the users' privacy settings allow us to view their rating, setup the event handler.
        if(user.getAverageRating() != -1)
        {
            findViewById(R.id.ProfileViewerActivityRatingTableRow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(IntentConstants.USER, gson.toJson(user));
                    startActivity(new Intent(ProfileViewerActivity.this, RatingsActivity.class).putExtras(bundle));
                }
            });
        }

        // Same goes for the journeys.
        if(user.getJourneysVisible())
        {
            findViewById(R.id.ProfileViewerActivityJourneysTableRow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ProfileViewerActivity.this, MyJourneysActivity.class).putExtra(IntentConstants.USER, gson.toJson(user)));
                }
            });
        }

        Button sendFriendRequestButton = (Button) findViewById(R.id.ProfileViewerActivitySendFriendRequestButton);
        sendFriendRequestButton.setEnabled(appManager.getUser().getUserId() != userId);
        sendFriendRequestButton.setVisibility(appManager.getUser().getUserId() == userId ? View.GONE : View.VISIBLE);
        sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileViewerActivity.this, SendFriendRequestActivity.class).putExtra(IntentConstants.USER, new Gson().toJson(user)));
            }
        });
    }

    /**
     * Start a new service task to retrieve this user's profile picture.
     */
    private void getProfilePicture()
    {
        new WcfPictureServiceTask(appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                userId, appManager.getAuthorisationHeaders(), this).execute();
    }

    /**
     * Called after the user's picture was successfully retrieved from the web service.
     *
     * @param bitmap - picture retrieved from the service in form of a bitmap.
     */
    @Override
    public void onImageRetrieved(Bitmap bitmap)
    {
        profileImageProgressBar.setVisibility(View.GONE);
        profileImageView.setVisibility(View.VISIBLE);

        if(bitmap != null)
        {
            BitmapDrawable icon = new BitmapDrawable(getResources() ,bitmap);
            actionBar.setIcon(icon);
            profileImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Called after user's information has been successfully retrieved from the web service.
     *
     * @param serviceResponse
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void parameter) {
        detailsRetriever = null;
        profileProgressBar.setVisibility(View.GONE);
        fillPersonDetails(serviceResponse.Result);
    }
}
