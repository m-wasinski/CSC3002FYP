package com.example.myapplication.activities.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.UpdateUserDTO;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.utilities.Utilities;
import com.example.myapplication.utilities.Validators;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This activity provides the user with the functionality to take a picture using the devices'
 * built in camera and upload it to the server as a new profile picture.
 **/
public class ProfileEditorActivity extends ProfileViewerActivity implements WCFServiceCallback<User, Void> {

    private TableRow emailAddressTableRow;
    private TableRow genderTableRow;
    private TableRow phoneNumberTableRow;
    private TableRow dateOfBirthTableRow;
    private TableRow nameTableRow;

    private ProgressBar progressBar;

    private static final String IMAGE_DIRECTORY_NAME = "FindNDrivePictures";

    private Uri fileUri;

    private final int CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final int PICK_IMAGE_FROM_GALLERY = 101;

    private User user;

    private Boolean setDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = appManager.getUser().getUserId();
        //Initialise UI elements.
        emailAddressTableRow = (TableRow) findViewById(R.id.ProfileViewerActivityEmailAddressTableRow);
        genderTableRow = (TableRow) findViewById(R.id.ProfileViewerActivityGenderTableRow);
        phoneNumberTableRow = (TableRow) findViewById(R.id.ProfileViewerActivityPhoneNumberTableRow);
        dateOfBirthTableRow = (TableRow) findViewById(R.id.ProfileViewerActivityDateOfBirthTableRow);
        nameTableRow = (TableRow) findViewById(R.id.ProfileViewerActivityNameTableRow);

        ImageView emailImageView = (ImageView) findViewById(R.id.ProfileViewerActivityEmailAddressArrowImageVIew);
        emailImageView.setVisibility(View.VISIBLE);

        ImageView genderImageView = (ImageView) findViewById(R.id.ProfileViewerActivityGenderImageView);
        genderImageView.setVisibility(View.VISIBLE);

        ImageView phoneImageView = (ImageView) findViewById(R.id.ProfileViewerActivityPhoneNumberImageView);
        phoneImageView.setVisibility(View.VISIBLE);

        ImageView dateOfBirthImageView = (ImageView) findViewById(R.id.ProfileViewerActivityDateOfBirthImageView);
        dateOfBirthImageView.setVisibility(View.VISIBLE);

        ImageView nameImageVIew = (ImageView) findViewById(R.id.ProfileViewerActivityNameArrowImageVIew);
        nameImageVIew.setVisibility(View.VISIBLE);

        phoneNumberTableRow.setClickable(true);
        genderTableRow.setClickable(true);
        emailAddressTableRow.setClickable(true);
        dateOfBirthTableRow.setClickable(true);
        nameTableRow.setClickable(true);

        progressBar = (ProgressBar) findViewById(R.id.ProfileViewerActivityProgressBar);

        profileImageView.setClickable(true);

        TextView tapToChangeTextView = (TextView) findViewById(R.id.ProfileViewerActivityTapToChangeTextView);
        tapToChangeTextView.setVisibility(View.VISIBLE);

        // Setup event handlers for UI elements.
        setupEventHandlers();
    }

    private void setupEventHandlers()
    {
        emailAddressTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmailAddress();
            }
        });

        genderTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGender();
            }
        });

        dateOfBirthTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateOfBirth();
            }
        });

        phoneNumberTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhoneNumber();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showProfilePictureOptionsMenu();
            }
        });

        nameTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getName();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                DialogFactory.getHelpDialog(this, "Your profile.", getResources().getString(R.string.MyProfileHelp));
                break;
            case R.id.action_home:
                startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.logout_menu_option:
                appManager.logout(true, true);
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.privacy_settings:
                startActivity(new Intent(this, PrivacySettingsActivity.class));
        }
        return false;
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
                        startGalleryActivity();
                        break;
                }
            }
        });
        genderDialog.show();
    }

    private void startGalleryActivity()
    {
        startActivityForResult(new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE_FROM_GALLERY);
    }

    private void getName()
    {
        final Dialog nameDialog = new Dialog(this);
        nameDialog.setContentView(R.layout.dialog_name_changer);
        nameDialog.setTitle("Enter new name.");

        final EditText firstNameEditText = (EditText) nameDialog.findViewById(R.id.NameChangerDialogFirstNameEditText);
        firstNameEditText.setText(appManager.getUser().getFirstName());

        final EditText lastNameEditText = (EditText) nameDialog.findViewById(R.id.NameChangerDialogLastNameEditText);
        lastNameEditText.setText(appManager.getUser().getLastName());

        Button okButton = (Button) nameDialog.findViewById(R.id.NameChangerDialogOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(new UpdateUserDTO(appManager.getUser().getUserId(), firstNameEditText.getText().toString(), lastNameEditText.getText().toString(),
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
                    saveChanges(new UpdateUserDTO(appManager.getUser().getUserId(), null, null, null, choice+1, null, null));
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
        emailEditText.setText(appManager.getUser().getEmailAddress());

        Button okButton = (Button) emailDialog.findViewById(R.id.ProfileEditorDialogOkButton);

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();

        if(accounts != null)
        {
            final ArrayList<String> emails = new ArrayList<String>();

            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches() && !emails.contains(account.name)) {
                    emails.add(account.name);
                }
            }

            if(emails.size() > 0)
            {
                TextView message = (TextView) emailDialog.findViewById(R.id.ProfileEditorDialogSelectFromTheFollowing);
                message.setVisibility(View.VISIBLE);
                message.setText("Or select from one of the following email addresses:");

                ListView listView = (ListView) emailDialog.findViewById(R.id.ProfileEditorDialogEmailsListView);
                listView.setVisibility(View.VISIBLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emails);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        newEmailAddressEntered(emails.get(i));
                    }
                });
            }

        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Validators.validateEmailAddress(emailEditText))
                {
                    newEmailAddressEntered(emailEditText.getText().toString());
                    emailDialog.dismiss();
                }
            }
        });

        emailDialog.show();
    }

    private void newEmailAddressEntered(String email)
    {
        memberEmailAddressTextView.setText(email);
        saveChanges(new UpdateUserDTO(appManager.getUser().getUserId(), null, null, email, -1, null, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void getPhoneNumber()
    {
        // Show the address dialog.
        final Dialog phoneNumberDialog = new Dialog(this);

        phoneNumberDialog.setContentView(R.layout.dialog_profile_editor);
        phoneNumberDialog.setTitle("Change phone number");

        final EditText phoneEditText = (EditText) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogEditText);
        phoneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        phoneEditText.setText(appManager.getUser().getPhoneNumber());

        Button okButton = (Button) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogOkButton);

        TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String phoneNumber = telephonyManager.getLine1Number();

        if(phoneNumber != null && !phoneNumber.isEmpty())
        {
            TextView message = (TextView) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogSelectFromTheFollowing);
            message.setVisibility(View.VISIBLE);
            message.setText("The following phone number was found on this device. Click on the number to select it.");

            final Button phoneNumberButton = (Button) phoneNumberDialog.findViewById(R.id.ProfileEditorDialogPhoneNumberButton);
            phoneNumberButton.setText(phoneNumber);
            phoneNumberButton.setVisibility(View.VISIBLE);
            phoneNumberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newPhoneNumberEntered(phoneNumberButton.getText().toString());
                }
            });
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                phoneNumberTextView.setText(phoneEditText.getText().toString());
                newPhoneNumberEntered(phoneEditText.getText().toString());
                phoneNumberDialog.dismiss();

            }
        });

        phoneNumberDialog.show();
    }

    private void newPhoneNumberEntered(String phoneNumber)
    {
        phoneNumberTextView.setText(phoneNumber);
        saveChanges(new UpdateUserDTO(appManager.getUser().getUserId(), null, null, null, -1, null, phoneNumber));
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
                    saveChanges(new UpdateUserDTO(appManager.getUser().getUserId(), null, null, null, -1, DateTimeHelper.convertToWCFDate(calendar.getTime()), null));
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
        progressBar.setVisibility(View.GONE);
        new WcfPostServiceTask<UpdateUserDTO>(this, getResources().getString(R.string.UpdateUserURL),
                updateUserDTO,
                new TypeToken<ServiceResponse<User>>() {}.getType(),
                appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void parameter) {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            appManager.setUser(serviceResponse.Result);
            user = serviceResponse.Result;
            super.fillPersonDetails(serviceResponse.Result);
            Toast toast = Toast.makeText(this, "Changes to your profile were saved successfully.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void startCameraActivity()
    {
        fileUri = getOutputMediaFileUri();
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, fileUri), CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillPersonDetails(appManager.getUser());
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
     * This method is called after user takes a picture with their
     * devices' camera and returns back to this activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                startActivity(new Intent(this, PictureUploadActivity.class).putExtra("file_uri", fileUri.toString()));

            }
            else if (resultCode == RESULT_CANCELED)
            {
                // User cancelled taking the picture.
                Toast.makeText(this,"Taking picture cancelled.", Toast.LENGTH_LONG).show();
            }
            else {
                // Error, failed to capture image.
                Toast.makeText(this,"Sorry! Failed to capture image", Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK)
        {
             Uri selectedImage = data.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
             Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
             cursor.moveToFirst();
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             String picturePath = cursor.getString(columnIndex);
             cursor.close();
             startActivity(new Intent(this, PictureUploadActivity.class).putExtra("file_uri",  picturePath));
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getBlankImageFile());
    }

    /**
     * Returns a blank image file from the devices image directory.
     * Devices camera then uses this file to write an image to it.
     * Partially referenced from:
     **/
    private File getBlankImageFile() {

        // Get a hold of the external storage location.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(IMAGE_DIRECTORY_NAME, "Error, failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date()) + ".jpg");
    }
}
