/*package com.algorhythms.infinity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by demouser on 6/12/13.
 */ /*
public class CreateTripActivity extends Activity {

    private TripsDbAdapter mTripsDbAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTripsDbAdapter = new TripsDbAdapter(this);
        setContentView(R.layout.trip_creation_layout);
    }


}*/

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.algorhythms.infinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

import com.algorhythms.util.Date;

/**
 * This activity is used to create a trip and place it in the database.
 */
public class CreateTripActivity extends Activity {

    private EditText mNameText; // The EditText field that the trip name is entered into
    private EditText mStartDateText; // The EditText field that the start date of the trip is entered into
    private EditText mEndDateText; // The EditText field that the end date of the trip is entered into
    private EditText mTripTypeText; // The EditText field that the trip type is entered into
    private Long mRowId;  //@todo Look into what this is used for, if used for editing, remove it and add it to edit activity
    private TripsDbAdapter mDbHelper; //db adapter used for connected to the trips table

    /**
     * Called when Activity beings, get all of the EditText fields and
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = TripsDbAdapter.getInstance(this);
        mDbHelper.open();

        setContentView(R.layout.trip_creation_layout);

        mNameText = (EditText) findViewById(R.id.tripNameBox);
        mStartDateText = (EditText) findViewById(R.id.startDateBox);
        mEndDateText = (EditText) findViewById(R.id.endDateBox);
        mTripTypeText = (EditText) findViewById(R.id.tripTypeBox);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(TripsDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(TripsDbAdapter.KEY_ROWID)
                    : null;
        }

        populateFields();
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor trip = mDbHelper.fetchTrip(mRowId);
            startManagingCursor(trip);
            mNameText.setText(trip.getString(
                    trip.getColumnIndexOrThrow(TripsDbAdapter.KEY_TRIPNAME)));
            mStartDateText.setText(trip.getString(
                    trip.getColumnIndexOrThrow(TripsDbAdapter.KEY_STARTDATE)));
            mEndDateText.setText(trip.getString(
                    trip.getColumnIndexOrThrow(TripsDbAdapter.KEY_ENDDATE)));
            mTripTypeText.setText(trip.getString(
                    trip.getColumnIndexOrThrow(TripsDbAdapter.KEY_TRIPTYPE)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(TripsDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (hasValidFields()) {
            saveState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    /**
     * @return True if the entered fields are valid; false otherwise.
     */
    private boolean hasValidFields() {
        String tripName = mNameText.getText().toString();
        String startDate = mStartDateText.getText().toString();
        String endDate = mEndDateText.getText().toString();

        if (tripName == null || tripName.trim().equals("")) {
            return false;
        }

        try {
            Date start = new Date(startDate);
            Date end = new Date(endDate);
            if (!start.isValid() || !end.isValid()) {
                return false;
            }
        }
        catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    private void saveState() {
        if (!hasValidFields()) {
            return;
        }

        String tripName = mNameText.getText().toString();
        String startDate = mStartDateText.getText().toString();
        String endDate = mEndDateText.getText().toString();
        String tripType = mTripTypeText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createTrip(tripName,startDate,endDate,tripType);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateTrip(mRowId,tripName,startDate,endDate,tripType);
        }
    }

    public void confirmTrip(View view) {
        String tripName = mNameText.getText().toString();
        String startDate = mStartDateText.getText().toString();
        String endDate = mEndDateText.getText().toString();

        // Name and trip type are mandatory
        if (tripName == null || tripName.trim().equals("")) {
            showAlert("You need a valid name for your trip!");
            return;
        }

        // Check the validity of dates
        try {
            Date start = new Date(startDate);
            Date end = new Date(endDate);
            if (!start.isValid()) {
                showAlert("Your start date is not a valid calendar day.");
                return;
            }
            if (!end.isValid()) {
                showAlert("Your end date is not a valid calendar day.");
                return;
            }

            if (Date.compare(start, end) > 0) {
                showAlert("The end date cannot be before the start date.");
                return;
            }
        }
        catch (IllegalArgumentException e) {
            showAlert("The date needs to be in the format MM/DD/YYYY.");
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Error!")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).show();
    }

}