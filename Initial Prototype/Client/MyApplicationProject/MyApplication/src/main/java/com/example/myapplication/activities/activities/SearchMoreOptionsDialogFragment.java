package com.example.myapplication.activities.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.myapplication.R;
import com.example.myapplication.dtos.JourneySearchDTO;
import com.example.myapplication.interfaces.OptionsDialogDismissListener;
import com.example.myapplication.utilities.DateTimeHelper;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michal on 26/02/14.
 */
public class SearchMoreOptionsDialogFragment extends DialogFragment implements View.OnClickListener {

    private JourneySearchDTO journeySearchDTO;
    private String[] options = new String[] {"I don't mind", "Yes", "No"};
    private String[] vehicleOptions;

    private boolean setDate;
    private boolean setTime;

    private Context context;
    private View view;

    private TableRow flexibleDateTableRow;
    private TableRow flexibleTimeTableRow;

    private TextView dateTextView;
    private TextView timeTextView;
    private TextView smokersTextView;
    private TextView petsTextView;
    private TextView vehicleTypeTextView;
    private TextView feeTextView;

    private EditText flexibleDateEditText;
    private EditText flexibleTimeEditText;

    private Button addFlexibleDateButton;
    private Button subtractFlexibleDateButton;
    private Button addFlexibleTimeButton;
    private Button subtractFlexibleTimeButton;

    private Calendar calendar;

    AlertDialog.Builder alertDialogBuilder;

    OptionsDialogDismissListener optionsDialogDismissListener;

    private sizeChangeListener sizeChangeListener;

    public SearchMoreOptionsDialogFragment(Context context, JourneySearchDTO journeySearchDTO, OptionsDialogDismissListener listner, sizeChangeListener sizeChangeListener)
    {

        this.context = context;
        this.journeySearchDTO = journeySearchDTO;
        this.calendar = Calendar.getInstance();

        if(this.journeySearchDTO.getDateAndTimeOfDeparture() != null)
        {
            this.calendar.setTime(DateTimeHelper.parseWCFDate(this.journeySearchDTO.getDateAndTimeOfDeparture()));
        }

        this.optionsDialogDismissListener = listner;
        this.sizeChangeListener = sizeChangeListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        alertDialogBuilder = new AlertDialog.Builder(this.context);
        this.view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_search_more_options, null);
        alertDialogBuilder.setView(view);
        this.vehicleOptions = concatenate(new String[] {"I don't mind"}, getResources().getStringArray(R.array.vehicle_types));

        this.flexibleDateTableRow = (TableRow) this.view.findViewById(R.id.SearchMoreOptionsFlexibleDateTableRow);
        this.flexibleDateTableRow.setVisibility(this.journeySearchDTO.getDepartureDate() == null ? View.GONE : View.VISIBLE);

        this.flexibleTimeTableRow = (TableRow) this.view.findViewById(R.id.SearchMoreOptionsFlexibleTimeTableRow);
        this.flexibleTimeTableRow.setVisibility(this.journeySearchDTO.getDepartureTime() == null ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.SearchMoreOptionsActivityDepartureDateTableRow).setOnClickListener(this);
        view.findViewById(R.id.SearchMoreOptionsActivityDepartureTimeTableRow).setOnClickListener(this);
        view.findViewById(R.id.SearchMoreOptionsActivitySmokersTableRow).setOnClickListener(this);
        view.findViewById(R.id.SearchMoreOptionsActivityPetsTableRow).setOnClickListener(this);
        view.findViewById(R.id.SearchMoreOptionsActivityVehicleTypeTableRow).setOnClickListener(this);
        view.findViewById(R.id.SearchMoreOptionsActivityFeeTimeTableRow).setOnClickListener(this);

        this.dateTextView = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivityDepartureDateTextView);
        this.dateTextView.setText(this.journeySearchDTO.getDepartureDate() == null ? "I don't mind" : DateTimeHelper.getSimpleDate(this.journeySearchDTO.getDateAndTimeOfDeparture()));

        this.timeTextView = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivityDepartureTimeTextView);
        this.timeTextView.setText(this.journeySearchDTO.getDepartureTime() == null ? "I don't mind" : DateTimeHelper.getSimpleTime(this.journeySearchDTO.getDateAndTimeOfDeparture()));

        this.smokersTextView  = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivitySmokersTextView);
        this.smokersTextView.setText(this.options[this.journeySearchDTO.getSmokers()]);

        this.petsTextView = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivityPetsTextView);
        this.petsTextView.setText(this.options[this.journeySearchDTO.getPets()]);

        this.vehicleTypeTextView = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivityVehicleTypeTextView);
        this.vehicleTypeTextView.setText(this.vehicleOptions[this.journeySearchDTO.getVehicleType()+1]);

        this.addFlexibleDateButton = (Button) view.findViewById(R.id.SearchMoreOptionsPlusDaysButton);
        this.addFlexibleDateButton.setOnClickListener(this);

        this.feeTextView = (TextView) this.view.findViewById(R.id.SearchMoreOptionsActivityFeeTimeTextView);
        this.feeTextView.setText(new DecimalFormat("0.00").format(this.journeySearchDTO.getFee()));

        this.subtractFlexibleDateButton = (Button) view.findViewById(R.id.SearchMoreOptionsMinusDaysButton);
        this.subtractFlexibleDateButton.setOnClickListener(this);
        this.subtractFlexibleDateButton.setEnabled(this.journeySearchDTO.getDateAllowance() > 0);

        this.addFlexibleTimeButton = (Button) view.findViewById(R.id.SearchMoreOptionsPlusHoursButton);
        this.addFlexibleTimeButton.setOnClickListener(this);


        this.subtractFlexibleTimeButton = (Button) view.findViewById(R.id.SearchMoreOptionsMinusHoursButton);
        this.subtractFlexibleTimeButton.setOnClickListener(this);
        this.subtractFlexibleTimeButton.setEnabled(this.journeySearchDTO.getTimeAllowance() > 0);

        this.flexibleDateEditText = (EditText) view.findViewById(R.id.SearchMoreOptionsFlexibleDateEditText);
        this.flexibleDateEditText.setText(String.valueOf(this.journeySearchDTO.getDateAllowance()));

        this.flexibleTimeEditText = (EditText) view.findViewById(R.id.SearchMoreOptionsFlexibleTimeEditText);
        this.flexibleTimeEditText.setText(String.valueOf(this.journeySearchDTO.getTimeAllowance()));

        return alertDialogBuilder.create();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.SearchMoreOptionsActivityDepartureDateTableRow:
                this.getDate(dateTextView);
                break;
            case R.id.SearchMoreOptionsActivityDepartureTimeTableRow:
                this.getTime(timeTextView);
                break;
            case R.id.SearchMoreOptionsActivitySmokersTableRow:
                showOptionsDialog(this.context, "Smokers", this.options, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        smokersTextView.setText(choice);
                        journeySearchDTO.setSmokers(which);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityPetsTableRow:
                showOptionsDialog(this.context, "Pets", this.options, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        petsTextView.setText(choice);
                        journeySearchDTO.setPets(which);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityVehicleTypeTableRow:
                showOptionsDialog(this.context, "Vehicle Type", this.vehicleOptions, new OnValueSelected() {
                    @Override
                    public void valueSelected(int which, String choice) {
                        vehicleTypeTextView.setText(choice);
                        journeySearchDTO.setVehicleType(which - 1);
                    }
                });
                break;
            case R.id.SearchMoreOptionsActivityFeeTimeTableRow:
                showFeeSpecifyDialog(new OnFeeSelected() {
                    @Override
                    public void feeSelected(double fee) {
                        feeTextView.setText(new DecimalFormat("0.00").format(fee));
                        journeySearchDTO.setFee(fee);
                    }
                });
                break;
            case R.id.SearchMoreOptionsPlusDaysButton:
                this.journeySearchDTO.setDateAllowance(this.journeySearchDTO.getDateAllowance()+1);
                this.flexibleDateEditText.setText(String.valueOf(this.journeySearchDTO.getDateAllowance()));
                this.subtractFlexibleDateButton.setEnabled(this.journeySearchDTO.getDateAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsMinusDaysButton:
                this.journeySearchDTO.setDateAllowance(this.journeySearchDTO.getDateAllowance() > 0 ? this.journeySearchDTO.getDateAllowance()-1 : 0);
                this.flexibleDateEditText.setText(String.valueOf(this.journeySearchDTO.getDateAllowance()));
                this.subtractFlexibleDateButton.setEnabled(this.journeySearchDTO.getDateAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsPlusHoursButton:
                this.journeySearchDTO.setTimeAllowance(this.journeySearchDTO.getTimeAllowance() + 1);
                this.flexibleTimeEditText.setText(String.valueOf(this.journeySearchDTO.getTimeAllowance()));
                this.subtractFlexibleTimeButton.setEnabled(this.journeySearchDTO.getTimeAllowance() > 0);
                break;
            case R.id.SearchMoreOptionsMinusHoursButton:
                this.journeySearchDTO.setTimeAllowance(this.journeySearchDTO.getTimeAllowance() > 0 ? this.journeySearchDTO.getTimeAllowance() - 1 : 0);
                this.flexibleTimeEditText.setText(String.valueOf(this.journeySearchDTO.getTimeAllowance()));
                this.subtractFlexibleTimeButton.setEnabled(this.journeySearchDTO.getTimeAllowance() > 0);
                break;
        }
    }

    private void getDate(final TextView textView)
    {
        final DatePickerDialog dateDialog = new DatePickerDialog(this.context, new DatePickerDialog.OnDateSetListener(){
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if(setDate)
                {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.UK);

                    textView.setText(simpleDateFormat.format(calendar.getTime()));
                    journeySearchDTO.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeySearchDTO.setDepartureDate(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeySearchDTO.setSearchByDate(true);
                    flexibleDateTableRow.setVisibility(View.VISIBLE);
                }

                sizeChangeListener.sizeChanged();
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

        dateDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "I don't mind", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDate = false;
                textView.setText("I don't mind");
                journeySearchDTO.setDepartureDate(null);
                flexibleDateTableRow.setVisibility(View.GONE);
                journeySearchDTO.setSearchByTime(false);
                journeySearchDTO.setDateAllowance(0);
            }
        });

        dateDialog.show();
    }

    private void getTime(final TextView textView)
    {
        final TimePickerDialog timeDialog = new TimePickerDialog(this.context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i2) {

                if(setTime)
                {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.UK);
                    textView.setText(sdf.format(calendar.getTime()));
                    journeySearchDTO.setDateAndTimeOfDeparture(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeySearchDTO.setDepartureTime(DateTimeHelper.convertToWCFDate(calendar.getTime()));
                    journeySearchDTO.setSearchByTime(true);
                    flexibleTimeTableRow.setVisibility(View.VISIBLE);
                }

                sizeChangeListener.sizeChanged();
            }
        }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true);

        timeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setDate = false;
                timeDialog.dismiss();
            }
        });

        timeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setTime = false;
                timeDialog.dismiss();
            }
        });

        timeDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Done", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user set new date
                setTime = true;
            }
        });

        timeDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "I don't mind", new DatePickerDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                setTime = false;
                textView.setText("I don't mind");
                journeySearchDTO.setDepartureTime(null);
                journeySearchDTO.setSearchByTime(false);
                journeySearchDTO.setTimeAllowance(0);
                flexibleTimeTableRow.setVisibility(View.GONE);
            }
        });

        timeDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        this.optionsDialogDismissListener.OnOptionsDialogDismiss(this.journeySearchDTO);
    }

    private void showFeeSpecifyDialog(final OnFeeSelected onFeeSelected)
    {
        final Dialog feeDialog = new Dialog(this.context);
        feeDialog.setContentView(R.layout.dialog_specify_fee);
        feeDialog.setTitle("Select fee");
        final EditText feeEditText = (EditText) feeDialog.findViewById(R.id.FeeSpecifyDialogFeeEditText);
        Button okButton = (Button) feeDialog.findViewById(R.id.FeeSpecifyDialogOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFeeSelected.feeSelected(parseFee(feeEditText));
                feeDialog.dismiss();
            }
        });

        Button freeButton = (Button) feeDialog.findViewById(R.id.FeeSpecifyDialogFreeButton);
        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFeeSelected.feeSelected(0);
                feeDialog.dismiss();
            }
        });

        feeDialog.show();
    }

    private double parseFee(EditText editText)
    {
        double fee;

        try
        {
            fee = Double.parseDouble(editText.getText().toString());

        }
        catch(NumberFormatException e)
        {
            fee = 0;
        }

        return fee;
    }

    private void showOptionsDialog(final Context context, String title, final String[] options, final OnValueSelected onValueSelectted)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setItems(options, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                onValueSelectted.valueSelected(which, options[which]);
            }
        });

        builder.show();
    }

    private interface OnValueSelected
    {
        void valueSelected(int which, String choice);
    }

    private interface OnFeeSelected
    {
        void feeSelected(double fee);
    }

    private static <T> T[] concatenate (T[] A, T[] B) {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(A.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }

    public interface sizeChangeListener
    {
        void sizeChanged();
    }
}
