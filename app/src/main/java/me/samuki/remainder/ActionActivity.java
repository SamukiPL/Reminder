package me.samuki.remainder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.MenuItemHoverListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ActionActivity extends Activity {
    private LinearLayout typeLayout;

    private Spinner typeSpinner;
    private int typeNumber;

    private RadioGroup dateOrPeriod;
    private TextView dateText;
    private EditText periodEdit;
    private Spinner periodSpinner;
    private int periodNumber;
    //Data format
    private SimpleDateFormat dateFormat;

    private ManagerDbAdapter database;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_activity);

        typeLayout = (LinearLayout) findViewById(R.id.typeLayout);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTimeLayout();
        setSpinner();
        setDateAndPeriods();

        database = new ManagerDbAdapter(this);
    }
    private boolean editIsEmpty(EditText edit) {
        if(edit.getText().toString().equals("")) {
            edit.setText("0");
            return true;
        }
        return false;
    }
    private boolean editIsEmpty(EditText edit, boolean test) {
        return edit.getText().toString().equals("");
    }
    private void zeroFirst(EditText edit, char first) {
        if(first == '0' && edit.isFocused())
            edit.setText("");
    }
    private void doneButton(EditText edit, int actionId) {
        if(actionId == EditorInfo.IME_ACTION_DONE) {
            edit.clearFocus();
            editIsEmpty(edit);
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        }
    }
    private void maxLengthCheck(EditText edit, EditText nextEdit, int length) {
        if(edit.getText().length() == length) {
            nextEdit.requestFocus();
        }
    }
    private void maxLengthCheck(EditText edit, int length) {
        if(edit.getText().length() == length) {
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            edit.clearFocus();
        }
    }
    private void setSpinner() {
        typeSpinner = (Spinner) findViewById(R.id.actionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.quantity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeInputType(parent.getItemAtPosition(position).toString());
                typeNumber = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void changeInputType(String type) {
        if(type.equals(getString(R.string.time)))
            setTimeLayout();
        else {
            setOtherTypeLayout(type);
        }
    }
    private void setTimeLayout() {
        typeLayout.removeAllViews();
        RelativeLayout hoursLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.time_layout, null);
        typeLayout.addView(hoursLayout);

        final EditText hours = (EditText) findViewById(R.id.hours);
        final EditText minutes = (EditText) findViewById(R.id.minutes);
        hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 1)
                    zeroFirst(hours, s.charAt(0));
                maxLengthCheck(hours, minutes, 2);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        hours.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    editIsEmpty(hours);
                    minutes.requestFocus();
                }
                return false;
            }
        });
        minutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editIsEmpty(hours);
                if(count == 1)
                    zeroFirst(minutes, s.charAt(0));
                maxLengthCheck(minutes, 2);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        minutes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doneButton(minutes, actionId);
                editIsEmpty(hours);
                return false;
            }
        });
    }
    private void setOtherTypeLayout(String type) {
        typeLayout.removeAllViews();
        LinearLayout otherLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.others_layout, null);
        typeLayout.addView(otherLayout);
        TextView otherText = (TextView) findViewById(R.id.otherTypeText);
        otherText.setText(type);
        final EditText otherEdit = (EditText) findViewById(R.id.otherType);

        otherEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 1)
                    zeroFirst(otherEdit, s.charAt(0));
                maxLengthCheck(otherEdit, 5);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otherEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doneButton(otherEdit, actionId);
                return false;
            }
        });
    }
    private void setDateAndPeriods() {
        //RADIO GROUP
        dateOrPeriod = (RadioGroup) findViewById(R.id.radioGroup);
        dateOrPeriod.check(R.id.until);
        //DATE
        dateText = (TextView) findViewById(R.id.dateText);
        dateFormat = new SimpleDateFormat("d-M-yyyy");
        dateText.setText(dateFormat.format(new Date().getTime()));
        //SPINNER ADAPTERS
        final ArrayAdapter<CharSequence> oneAdapter = ArrayAdapter.createFromResource(this,
                R.array.one, android.R.layout.simple_spinner_item);
        oneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<CharSequence> pluralAdapter = ArrayAdapter.createFromResource(this,
                R.array.plural, android.R.layout.simple_spinner_item);
        pluralAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //PERIOD
        periodEdit = (EditText) findViewById(R.id.periodEdit);
        periodSpinner = (Spinner) findViewById(R.id.periodSpinner);
        periodNumber = 0;

        periodSpinner.setAdapter(oneAdapter);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodNumber = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        periodEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int position = periodSpinner.getSelectedItemPosition();

                if(count == 1)
                    zeroFirst(periodEdit, s.charAt(0));

                if(s.toString().equals("1"))
                    periodSpinner.setAdapter(oneAdapter);
                else
                    periodSpinner.setAdapter(pluralAdapter);

                periodSpinner.setSelection(position);
                maxLengthCheck(periodEdit, 2);
                dateOrPeriod.check(R.id.repeat);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        periodEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doneButton(periodEdit, actionId);
                return false;
            }
        });

    }
    public void calendarPopup(View view) {
        LinearLayout calendarLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.calendar_alert, null);
        final DatePicker dataPicker = (DatePicker) calendarLayout.getChildAt(0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dataPicker.setMinDate(calendar.getTimeInMillis());

        try {
            calendar.setTime(dateFormat.parse(dateText.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dataPicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dateText.setText(getString(R.string.date, dataPicker.getDayOfMonth(), dataPicker.getMonth()+1, dataPicker.getYear()));
                        dateOrPeriod.check(R.id.until);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setTitle(R.string.calendarTitle);
        alertDialog.setView(calendarLayout);
        alertDialog.show();
    }
    private long amountChanger() {
        long amount;
        if(typeNumber == 0) {
            EditText hoursEdit = (EditText) findViewById(R.id.hours);
                editIsEmpty(hoursEdit);
            EditText minutesEdit = (EditText) findViewById(R.id.minutes);
                editIsEmpty(minutesEdit);
            long hours = Long.parseLong(hoursEdit.getText().toString());
            long minutes = Long.parseLong(minutesEdit.getText().toString());
            amount = (hours*3600000)+(minutes*60000);
        }
        else {
            EditText amountEdit = (EditText) findViewById(R.id.otherType);
                editIsEmpty(amountEdit);
            amount = Long.parseLong(amountEdit.getText().toString());
        }
        return amount;
    }
    private void fillFormYouMoron() {
        Toast.makeText(this, "You have to fill all required things!", Toast.LENGTH_SHORT).show();
    }
    public void addAction(View view) {
        EditText actionName = (EditText) findViewById(R.id.actionName);
        EditText periodEdit = (EditText) findViewById(R.id.periodEdit);
        //TYPE AMOUNT CHANGER

        String name = actionName.getText().toString();
        String type = typeSpinner.getSelectedItem().toString();
        long amount = amountChanger();
        String date = dateText.getText().toString();

        if(!editIsEmpty(actionName, true) && amount != 0 && !editIsEmpty(periodEdit, true) || dateOrPeriod.getCheckedRadioButtonId() == R.id.until) {
            database.open();
            if (dateOrPeriod.getCheckedRadioButtonId() == R.id.until) {
                database.insertAction(name, type, amount, date);
            } else if(dateOrPeriod.getCheckedRadioButtonId() == R.id.repeat){;
                Calendar cal = Calendar.getInstance();
                date = dateFormat.format(cal.getTime());
                int repeat = Integer.parseInt(periodEdit.getText().toString());
                String often = periodSpinner.getItemAtPosition(periodNumber).toString();
                database.insertAction(name, type, amount, date, repeat, often);
            }
            database.close();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("AfterAddingAction", "Added");
            startActivity(intent);
        }
        else
            fillFormYouMoron();

    }
}
