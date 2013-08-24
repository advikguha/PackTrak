package com.algorhythms.infinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class NewItemActivity extends Activity {
	public static final String NEW_ITEM_NAME="new_item_name";
	private ItemDbAdapter mDbHelper;
	private String defaultText = "No information provided";
	
    private EditText mNameText; // The EditText field where item name goes
    private EditText mLocationHint; // The EditText field where the location hint of item goes
    private EditText mMoreInfo; // The EditText field where more info about item goes
    private EditText mQuantity; // The EditText field where the amount of this item required goes
	private Long mCatId; // The unique id of the category that this item is a part of
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_item);
		mCatId=getIntent().getExtras().getLong(DrawerScreenActivity.categoryIdKey);
		
	}
	
	public void createItem(View view){
		mNameText= (EditText) findViewById(R.id.itemName_editText);
		String itemName = mNameText.getText().toString();
		mQuantity = (EditText) findViewById(R.id.itemQuantity_editText);
		String itemQuantity = mQuantity.getText().toString();
		mLocationHint = (EditText) findViewById(R.id.locationHint_editText);
		String locationHint = mLocationHint.getText().toString();
		mMoreInfo = (EditText) findViewById(R.id.extraNotes_editText);
		String moreInfo = mMoreInfo.getText().toString();
		if(!itemName.equals("")){
			Intent returnIntent= new Intent();
			//returnIntent.putExtra(NEW_ITEM_NAME, itemName);
			mDbHelper=ItemDbAdapter.getInstance(this);
			mDbHelper.open();
			mDbHelper.createItem(itemName, mCatId, 1,(locationHint.equals("")? defaultText:locationHint), (moreInfo.equals("")?defaultText:moreInfo));
			setResult(RESULT_OK, returnIntent);
			//mDbHelper.close();
			finish();
		}
		else{
			Intent returnIntent= new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
	}
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_item, menu);
		return true;
	}*/
}
