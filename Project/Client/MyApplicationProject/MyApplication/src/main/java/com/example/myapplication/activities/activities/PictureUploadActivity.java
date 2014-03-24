package com.example.myapplication.activities.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.ProfilePictureUpdaterDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.BitmapUtilities;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;

/**
 * Provides functionality to resize, rescale and upload user's new profile picture to the server.
 **/
public class PictureUploadActivity extends BaseActivity implements View.OnClickListener {

    private Button okButton;
    private Button cancelButton;

    private ProgressBar progressBar;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_picture_confirm);

        // Initialise local variables.
        Uri fileUri = Uri.parse(getIntent().getStringExtra("file_uri"));

        // Initialise UI elements and setup their event handlers..
        ImageView imageView = (ImageView) this.findViewById(R.id.PictureConfirmActivityPictureImageView);
        this.okButton = (Button) this.findViewById(R.id.PictureConfirmActivityOKButton);
        this.cancelButton = (Button) this.findViewById(R.id.PictureConfirmActivityCancelButton);
        this.progressBar = (ProgressBar) this.findViewById(R.id.PictureConfirmActivityProgressBar);

        this.okButton.setOnClickListener(this);
        this.cancelButton.setOnClickListener(this);

        // Decode and resize the bitmap while maintaining aspect ratio.
        int BITMAP_WIDTH = 270;
        int BITMAP_HEIGHT = 330;

        this.bitmap = Bitmap.createScaledBitmap(BitmapUtilities.decodeFile(fileUri.getPath(), BITMAP_WIDTH, BITMAP_HEIGHT, BitmapUtilities.ScalingLogic.CROP), BITMAP_WIDTH, BITMAP_HEIGHT, false);
        imageView.setImageBitmap(this.bitmap);
    }



    /**
     * Converts the bitmap variable into a base64 String and uploads it to the
     * WCF service which in turn updates the database record for this user's profile picture.
     * Base64 String is used to enable the bitmap being transferred inside a JSON object.
     **/
    private void uploadImage()
    {
        //Convert the bitmap to byte array.
        this.progressBar.setVisibility(View.VISIBLE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream);
        byte[] byteArray = stream.toByteArray();

        //Just before sending the bitmap, convert it to byte64 string to allow it to be transferred inside JSON object.
        new WcfPostServiceTask<ProfilePictureUpdaterDTO>(this, getResources().getString(R.string.UpdateProfilePictureURL),
                new ProfilePictureUpdaterDTO(this.appManager.getUser().getUserId(), Base64.encodeToString(byteArray, Base64.DEFAULT)),
                new TypeToken<ServiceResponse<Void>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Void, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    pictureUploadedSuccessfully(bitmap);
                }
            }
        }).execute();
    }

    /**
     * Called when the picture was uploaded successfully.
     **/
    private void pictureUploadedSuccessfully(Bitmap bitmap)
    {
        //Add this picture tp the global lru cache.
        this.appManager.getBitmapLruCache().put(String.valueOf(this.appManager.getUser().getUserId()),bitmap);
        this.progressBar.setVisibility(View.GONE);
        this.finish();
    }

    /**
     * Fired when one of the buttons is in this activity is clicked.
     * We use switch statement to detect which button has been clicked.
     **/
    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.PictureConfirmActivityOKButton:
                this.uploadImage();
                break;
            case R.id.PictureConfirmActivityCancelButton:
                this.finish();
                break;
        }
    }



}
