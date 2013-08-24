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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

/**
 *   This Activity is used for allowing the user to input information about an item they wish to create and then
 *   actually creates that item in the database.
 */
public class CreateItemActivity extends Activity {

    private EditText mNameText; // The EditText field where item name goes
    private EditText mLocationHint; // The EditText field where the location hint of item goes
    private EditText mMoreInfo; // The EditText field where more info about item goes
    private EditText mQuantity; // The EditText field where the amount of this item required goes
    private ItemDbAdapter mDbHelper; // The db adapter used to connect to the items table of the database
    private Long mCatId; // The unique id of the category that this item is a part of


    /**
     * When this activity is created, get a hold of all of the EditText views and attempt to retrieve the
     * categoryID both from the intent that started this activity and from the savedInstance state passed
     * into this method as a parameter.
     *
     * @param savedInstanceState If the user minimizes app, this is where the information is saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_creation_layout);
        mDbHelper = ItemDbAdapter.getInstance(this);
        mDbHelper.open();

        // Get the EditText views using their corresponding ids
        mNameText = (EditText) findViewById(R.id.itemNameBox);
        mLocationHint = (EditText) findViewById(R.id.locationHintBox);
        mMoreInfo = (EditText) findViewById(R.id.itemInfoBox);
        mQuantity = (EditText) findViewById(R.id.itemQuantityBox);

        Bundle extras = getIntent().getExtras();

        // Check the serializables for the cat id
        mCatId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(CategoryViewActivity.categoryIdKey);

        // If didn't find it there, check the extras for the catefory id
        if(mCatId == null) {
            // Get the category Id
            mCatId = extras != null ? extras.getLong(CategoryViewActivity.categoryIdKey) : null;

            // If category Id is null some error has occured, finish this activity
            if(mCatId == null)
                finish();
        }

        // Open up the category db adapter
        CategoryDbAdapter categoryAdapter = CategoryDbAdapter.getInstance(this);
        categoryAdapter.open();

        // Using this category db adapter, change the name of the title of this activity to display the category name
        Cursor item = categoryAdapter.fetchCategory(mCatId);
        startManagingCursor(item);
        TextView title = (TextView)findViewById(R.id.itemHeader);
        title.setText("Add item to " + item.getString(item.getColumnIndexOrThrow(CategoryDbAdapter.KEY_CATNAME)));
    }

    /**
     * @todo: Find out when this method is called
     * Saves the category id in the savedInstanceState to later be loaded back in when the app is loaded again
     * and also saves the values of the current EditText fields
     *
     * @param outState  The Bundle that is used to save the information
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // If the EditTextFields contain valid information, save them, this may have to be changed?
        if(hasValidFields()) {
            saveState();
            outState.putSerializable(CategoryViewActivity.categoryIdKey, mCatId);
        }
    }

    /**
     * @todo: Find out when this method is called
     *
     * Saves the values of the edittext fields
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(hasValidFields())
            saveState();
    }

    /**
     * @todo Find out when this method is called
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * This method can only be called if the EditText boxes contain valid fields, creates a new item in the database
     * with information received from the EditText fields
     */
    private void saveState() {
        String itemName = mNameText.getText().toString();
        String locationHint = mLocationHint.getText().toString();
        long quantity = Long.parseLong(mQuantity.getText().toString());
        String info = mMoreInfo.getText().toString();

        mDbHelper.createItem(itemName, mCatId, quantity, locationHint, info);
    }

    /**
     * This method is fired off when the create item button is clicked.  Check that all of the fields
     * for the item are valid, otherwise tell the user that they need to change whatever field is invalid.
     *
     * @param view The button that was clicked
     */
    public void confirmItem(View view) {
        String itemName = mNameText.getText().toString();

        if(itemName == null || "".equals(itemName.trim())) {
            showAlert("You need a valid name for your item!");
            return;
        }

        try {
            Integer.parseInt(mQuantity.getText().toString());
        }
        catch(NumberFormatException e) {
            showAlert("You need to enter a number for the quantity!");
            return;
        }


        setResult(RESULT_OK);
        finish();
    }

    /**
     * @return True if the entered fields are valid; false otherwise.
     */
    private boolean hasValidFields() {
        String itemName = mNameText.getText().toString();

        if (itemName == null || itemName.trim().equals("")) {
            return false;
        }

        try {
            String quantity = mQuantity.getText().toString();
            Integer.parseInt(quantity);
        }
        catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Show an alert to the user
     *
     * @param msg The message to display to the user
     */
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