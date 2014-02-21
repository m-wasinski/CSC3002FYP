package com.example.myapplication.activities.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;

/**
 * Created by Michal on 20/02/14.
 */
public class PictureUploadActivity extends BaseActivity {

    private Button okButton;
    private Button cancelButton;

    private ImageView imageView;

    private Uri fileUri;

    private ProgressBar progressBar;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_picture_confirm);

        // Initialise local variables.
        this.fileUri = Uri.parse(getIntent().getStringExtra("file_uri"));

        // Initialise UI elements.
        this.imageView = (ImageView) this.findViewById(R.id.PictureConfirmActivityPictureImageView);
        this.okButton = (Button) this.findViewById(R.id.PictureConfirmActivityOKButton);
        this.cancelButton = (Button) this.findViewById(R.id.PictureConfirmActivityCancelButton);
        this.progressBar = (ProgressBar) this.findViewById(R.id.PictureConfirmActivityProgressBar);

        // Retrieve the image from URI and load it into the imageview.
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing image as it throws OutOfMemory Exception for larger ones.
        options.inSampleSize = 8;

        this.bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

        this.imageView.setImageBitmap(bitmap);

        // Setup event handlers.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okButton.setEnabled(false);
                uploadImage();
            }
        });

        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void uploadImage()
    {
        this.progressBar.setVisibility(View.VISIBLE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream);
        byte[] byteArray = stream.toByteArray();

        new WcfPostServiceTask<ProfilePictureUpdaterDTO>(this, getResources().getString(R.string.UpdateProfilePictureURL),
                new ProfilePictureUpdaterDTO(this.findNDriveManager.getUser().getUserId(), Base64.encodeToString(byteArray, Base64.DEFAULT)),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), new WCFServiceCallback() {
            @Override
            public void onServiceCallCompleted(ServiceResponse serviceResponse, Object parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    pictureUploadedSuccessfully();
                }
            }
        }).execute();
    }

    private void pictureUploadedSuccessfully()
    {
        okButton.setEnabled(true);
        this.progressBar.setVisibility(View.GONE);
        this.finish();
    }

}
