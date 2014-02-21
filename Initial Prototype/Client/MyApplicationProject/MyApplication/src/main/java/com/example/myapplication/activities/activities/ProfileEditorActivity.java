package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.constants.GenderTypes;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.UpdateUserDTO;
import com.example.myapplication.experimental.DateTimeHelper;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Utilities;
import com.example.myapplication.utilities.Validators;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Michal on 19/02/14.
 */
public class ProfileEditorActivity extends ProfileViewerActivity implements WCFServiceCallback<User, Void> {

    private TableRow emailAddressTableRow;
    private TableRow genderTableRow;
    private TableRow phoneNumberTableRow;
    private TableRow dateOfBirthTableRow;
    private TableRow nameTableRow;

    private ImageView emailImageView;
    private ImageView genderImageView;
    private ImageView phoneImageView;
    private ImageView dateOfBirthImageView;
    private ImageView nameImageVIew;

    private TextView tapToChangeTextView;

    private ProgressBar progressBar;

    private static final String IMAGE_DIRECTORY_NAME = "FindNDrivePictrures";

    private Uri fileUri;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private Boolean setDate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialise UI elements.
        this.emailAddressTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityEmailAddressTableRow);
        this.genderTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityGenderTableRow);
        this.phoneNumberTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityPhoneNumberTableRow);
        this.dateOfBirthTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityDateOfBirthTableRow);
        this.nameTableRow = (TableRow) this.findViewById(R.id.ProfileViewerActivityNameTableRow);

        this.emailImageView = (ImageView) this.findViewById(R.id.ProfileViewerActivityEmailAddressArrowImageVIew);
        this.emailImageView.setVisibility(View.VISIBLE);

        this.genderImageView = (ImageView) this.findViewById(R.id.ProfileViewerActivityGenderImageView);
        this.genderImageView.setVisibility(View.VISIBLE);

        this.phoneImageView = (ImageView) this.findViewById(R.id.ProfileViewerActivityPhoneNumberImageView);
        this.phoneImageView.setVisibility(View.VISIBLE);

        this.dateOfBirthImageView = (ImageView) this.findViewById(R.id.ProfileViewerActivityDateOfBirthImageView);
        this.dateOfBirthImageView.setVisibility(View.VISIBLE);

        this.nameImageVIew = (ImageView) this.findViewById(R.id.ProfileViewerActivityNameArrowImageVIew);
        this.nameImageVIew.setVisibility(View.VISIBLE);

        this.phoneNumberTableRow.setClickable(true);
        this.genderTableRow.setClickable(true);
        this.emailAddressTableRow.setClickable(true);
        this.dateOfBirthTableRow.setClickable(true);
        this.nameTableRow.setClickable(true);

        this.progressBar = (ProgressBar) this.findViewById(R.id.ProfileViewerActivityProgressBar);

        this.profileImageView.setClickable(true);

        this.tapToChangeTextView = (TextView) this.findViewById(R.id.ProfileViewerActivityTapToChangeTextView);
        this.tapToChangeTextView.setVisibility(View.VISIBLE);

        // Setup event handlers for UI elements.
        this.setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        this.emailAddressTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmailAddress();
            }
        });

        this.genderTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGender();
            }
        });

        this.dateOfBirthTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateOfBirth();
            }
        });

        this.phoneNumberTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhoneNumber();
            }
        });

        this.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showProfilePictureOptionsMenu();
            }
        });

        this.nameTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getName();
            }
        });
    }

    private void showProfilePictureOptionsMenu()
    {
        final String items[] = {"Camera","Gallery"};

        AlertDialog.Builder genderDialog = new AlertDialog.Builder(this);
        genderDialog.setTitle("Change profile picture");
        genderDialog.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int choice) {
                switch (choice)
                {
                    case 0:
                        startCameraActivity();
                        break;
                    case 1:

                        break;
                }
            }
        });
        genderDialog.show();
    }

    private void getName()
    {
        final Dialog nameDialog = new Dialog(this);
        nameDialog.setContentView(R.layout.dialog_name_changer);
        nameDialog.setTitle("Enter new name.");

        final EditText firstNameEditText = (EditText) nameDialog.findViewById(R.id.NameChangerDialogFirstNameEditText);
        firstNameEditText.setText(this.findNDriveManager.getUser().getFirstName());

        final EditText lastNameEditText = (EditText) nameDialog.findViewById(R.id.NameChangerDialogLastNameEditText);
        lastNameEditText.setText(this.findNDriveManager.getUser().getLastName());

        Button okButton = (Button) nameDialog.findViewById(R.id.NameChangerDialogOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(new UpdateUserDTO(findNDriveManager.getUser().getUserId(), firstNameEditText.getText().toString(), lastNameEditText.getText().toString(),
                        null, -1, null, null));
                nameDialog.dismiss();
            }
        });

        nameDialog.show();
    }

    private void getGender()
    {
        final String items[] = {"Male","Female"};

        AlertDialog.Builder genderDialog = new AlertDialog.Builder(this);
        genderDialog.setTitle("Select gender");
        genderDialog.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int choice) {

                if(choice == 0 || choice == 1)
                {
                    memberGenderTextView.setText(Utilities.translateGender(choice+1));
                    saveChanges(new UpdateUserDTO(findNDriveManager.getUser().getUserId(), null, null, null, choice+1, null, null));
                }
            }
        });
        genderDialog.show();
    }

    private void getEmailAddress()
    {
        // Show the address dialog.
        final Dialog emailDialog = new Dialog(this);

        emailDialog.setContentView(R.layout.dialog_profile_editor);
        emailDialog.setTitle("Change email address");

        final EditText emailEditText = (EditText) emailDialog.findViewById(R.id.ProfileEditorDialogEditText);
        emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEditText.setText(this.findNDriveManager.getUser().getEmailAddress());

        Button okButton = (Button) emailDialog.findViewById(R.id.ProfileEditorDialogOkButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Validators.validateEmailAddress(emailEditText))
                {
                    memberEmailAddressTextView.setText(emailEditText.getText().toString());
                    saveChanges(new UpdateUserDTO(findNDriveManager.getUser().getUserId(), null, null, emailEditText.getText().toString(), -1, null, null));
                    emailDialog.dismiss();
                }
            }
        });

        emailDialog.show();
    }

    private void getPhoneNumber()
    {
        // Show the address dialog.
        final Dialog phoneNumberDialog = new Dialog(this);

        phoneNumberDialog.setContentView(R.layout.dialog_profile_editor);
        phoneNumberDialog.setTitle("Change phone number");

        final EditText phoneEditText = (EditText) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogEditText);
        phoneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        phoneEditText.setText(this.findNDriveManager.getUser().getPhoneNumber());

        Button okButton = (Button) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogOkButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                phoneNumberTextView.setText(phoneEditText.getText().toString());
                saveChanges(new UpdateUserDTO(findNDriveManager.getUser().getUserId(), null, null, null, -1, null, phoneNumberTextView.getText().toString()));
                phoneNumberDialog.dismiss();

            }
        });

        phoneNumberDialog.show();
    }

    private void getDateOfBirth()
    {
        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if(setDate)
                {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);

                    dateOfBirthTextVIew.setText(simpleDateFormat.format(calendar.getTime()));
                    saveChanges(new UpdateUserDTO(findNDriveManager.getUser().getUserId(), null, null, null, -1, DateTimeHelper.convertToWCFDate(calendar.getTime()), null));
                }

            }
        } ,calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setDate = false;
                dateDialog.dismiss();
            }
        });

        dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setDate = false;
                dateDialog.dismiss();
            }
        });

        dateDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Done", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user set new date
                setDate = true;
            }
        });

        dateDialog.show();
    }

    private void saveChanges(UpdateUserDTO updateUserDTO) {
        this.progressBar.setVisibility(View.GONE);
        new WcfPostServiceTask<UpdateUserDTO>(this, getResources().getString(R.string.UpdateUserURL),
                updateUserDTO,
                new TypeToken<ServiceResponse<User>>() {}.getType(),
                findNDriveManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void parameter) {
        this.progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            this.findNDriveManager.setUser(serviceResponse.Result);
            this.user = serviceResponse.Result;
            super.fillPersonDetails();
            Toast toast = Toast.makeText(this, "Changes to your profile were saved successfully.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void startCameraActivity()
    {
        this.fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        this.startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, fileUri), CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK)
            {
                this.startActivity(new Intent(this, PictureUploadActivity.class).putExtra("file_uri", fileUri.toString()));

            } else if (resultCode == RESULT_CANCELED) {
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());

        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
