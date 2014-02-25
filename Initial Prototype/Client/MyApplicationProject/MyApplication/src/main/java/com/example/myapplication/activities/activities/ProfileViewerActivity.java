package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Michal on 19/02/14.
 */
public class ProfileViewerActivity extends BaseActivity implements WCFImageRetrieved {

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

    private TableRow ratingsTableRow;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getProfilePicture();
        this.fillPersonDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_profile_viewer);

        // Initialise local variables.
        this.user = gson.fromJson(getIntent().getStringExtra(IntentConstants.USER), new TypeToken<User>() {}.getType());
        this.setTitle(this.user.getUserName());

        //Initialise UI elements.
        this.memberNameTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityNameTextView);
        this.memberEmailAddressTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityEmailTextView);
        this.memberGenderTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityGenderTextView);
        this.memberSinceTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityMemberSinceTextView);
        this.dateOfBirthTextVIew = (TextView) this.findViewById(R.id.ProfileViewerActivityDateOfBirthTextView);
        this.phoneNumberTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityPhoneNumberTextView);
        this.lastLogonTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityLastLogonTextView);
        this.ratingTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityRatingTextView);

        this.profileImageView = (ImageView) this.findViewById(R.id.ProfileViewerActivityProfileIconImageView);

        this.profileImageProgressBar = (ProgressBar) this.findViewById(R.id.ProfileViewerActivityProfileImageProgressBar);

        this.ratingsTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityRatingTableRow);

        // Setup all event handlers.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.ratingsTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingsActivity();
            }
        });
    }

    private void showRatingsActivity()
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.USER, gson.toJson(this.user));
        this.startActivity(new Intent(this, RatingsActivity.class).putExtras(bundle));
    }


    protected void fillPersonDetails()
    {
        this.memberNameTextView.setText(this.user.getFirstName() + " " + this.user.getLastName() + " (" + this.user.getUserName()+")");
        this.memberEmailAddressTextView.setText(this.user.getEmailAddress());
        this.memberGenderTextView.setText(this.user.getGender() == 0 ? "N/A" : Utilities.translateGender(this.user.getGender()));
        this.memberSinceTextView.setText(DateTimeHelper.getSimpleDate(this.user.getMemberSince()));
        this.dateOfBirthTextVIew.setText(this.user.getDateOfBirth() == null ? "N/A" : DateTimeHelper.getSimpleDate(this.user.getDateOfBirth()));
        this.phoneNumberTextView.setText(this.user.getPhoneNumber() == null ? "N/A" : this.user.getPhoneNumber());
        this.lastLogonTextView.setText(DateTimeHelper.getSimpleDate(this.user.getLastLogon()) + " " + DateTimeHelper.getSimpleTime(this.user.getLastLogon()));
        this.ratingTextView.setText(String.valueOf(String.valueOf(this.user.getAverageRating())));
    }

    private void getProfilePicture()
    {
        new WcfPictureServiceTask(this.appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                this.user.getUserId(), this.appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onImageRetrieved(Bitmap bitmap)
    {
        this.profileImageProgressBar.setVisibility(View.GONE);
        this.profileImageView.setVisibility(View.VISIBLE);

        if(bitmap != null)
        {
            this.profileImageView.setImageBitmap(bitmap);
        }
    }
}
