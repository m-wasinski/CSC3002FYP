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
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 19/02/14.
 */
public class ProfileViewerActivity extends BaseActivity implements WCFImageRetrieved, View.OnClickListener {

    protected User user;

    protected TextView memberNameTextView;
    protected TextView memberEmailAddressTextView;
    protected TextView memberGenderTextView;
    protected TextView memberSinceTextView;
    protected TextView dateOfBirthTextVIew;
    protected TextView phoneNumberTextView;
    protected TextView lastLogonTextView;
    protected TextView ratingTextView;

    protected ImageView profileImageView;

    private ProgressBar profileImageProgressBar;

    private Button sendFriendRequestButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProfilePicture();
        fillPersonDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_viewer);

        // Initialise local variables.
        user = gson.fromJson(getIntent().getStringExtra(IntentConstants.USER), new TypeToken<User>() {}.getType());
        setTitle(user.getUserName());

        //Initialise UI elements.
        memberNameTextView = (TextView) findViewById(R.id.ProfileViewerActivityNameTextView);
        memberEmailAddressTextView = (TextView) findViewById(R.id.ProfileViewerActivityEmailTextView);
        memberGenderTextView = (TextView) findViewById(R.id.ProfileViewerActivityGenderTextView);
        memberSinceTextView = (TextView) findViewById(R.id.ProfileViewerActivityMemberSinceTextView);
        dateOfBirthTextVIew = (TextView) findViewById(R.id.ProfileViewerActivityDateOfBirthTextView);
        phoneNumberTextView = (TextView) findViewById(R.id.ProfileViewerActivityPhoneNumberTextView);
        lastLogonTextView = (TextView) findViewById(R.id.ProfileViewerActivityLastLogonTextView);
        ratingTextView = (TextView) findViewById(R.id.ProfileViewerActivityRatingTextView);
        sendFriendRequestButton = (Button) findViewById(R.id.ProfileViewerActivitySendFriendRequestButton);
        sendFriendRequestButton.setEnabled(appManager.getUser().getUserId() != user.getUserId());
        sendFriendRequestButton.setVisibility(appManager.getUser().getUserId() == user.getUserId() ? View.GONE : View.VISIBLE);
        sendFriendRequestButton.setOnClickListener(this);
        profileImageView = (ImageView) findViewById(R.id.ProfileViewerActivityProfileIconImageView);

        profileImageProgressBar = (ProgressBar) findViewById(R.id.ProfileViewerActivityProfileImageProgressBar);

        findViewById(R.id.ProfileViewerActivityRatingTableRow).setOnClickListener(this);
        findViewById(R.id.ProfileViewerActivityJourneysTableRow).setOnClickListener(this);
        ((TextView)findViewById(R.id.ProfileViewerActivityJourneysTextView)).setText("View " + user.getUserName() + "'s journeys.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogCreator.showHelpDialog(this, user.getUserName(), getResources().getString(R.string.ProfileViewerHelp));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void fillPersonDetails()
    {
        memberNameTextView.setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getUserName()+")");
        memberEmailAddressTextView.setText(user.getEmailAddress());
        memberGenderTextView.setText(user.getGender() == 0 ? "N/A" : Utilities.translateGender(user.getGender()));
        memberSinceTextView.setText(DateTimeHelper.getSimpleDate(user.getMemberSince()));
        dateOfBirthTextVIew.setText(user.getDateOfBirth() == null ? "N/A" : DateTimeHelper.getSimpleDate(user.getDateOfBirth()));
        phoneNumberTextView.setText(user.getPhoneNumber() == null ? "N/A" : user.getPhoneNumber());
        lastLogonTextView.setText(DateTimeHelper.getSimpleDate(user.getLastLogon()) + " " + DateTimeHelper.getSimpleTime(user.getLastLogon()));
        ratingTextView.setText(String.valueOf(String.valueOf(user.getAverageRating())));
    }

    private void getProfilePicture()
    {
        new WcfPictureServiceTask(appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                user.getUserId(), appManager.getAuthorisationHeaders(), this).execute();
    }

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

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.ProfileViewerActivityJourneysTableRow:
                startActivity(new Intent(this, MyJourneysActivity.class).putExtra(IntentConstants.USER, gson.toJson(user)));
                break;
            case R.id.ProfileViewerActivityRatingTableRow:
                Bundle bundle = new Bundle();
                bundle.putString(IntentConstants.USER, gson.toJson(user));
                startActivity(new Intent(this, RatingsActivity.class).putExtras(bundle));
                break;
            case R.id.ProfileViewerActivitySendFriendRequestButton:
                startActivity(new Intent(this, SendFriendRequestDialogActivity.class).putExtra(IntentConstants.USER, new Gson().toJson(user)));
                break;
        }
    }
}
