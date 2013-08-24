package com.algorhythms.infinity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class NewCategoryActivity extends Activity {

	public static final String NEW_CAT="new_category_name";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_category);
		
	}
	public void createCategory(View view){
		EditText textbox = (EditText) findViewById(R.id.edit_text1);
		String message = textbox.getText().toString();
		if(!message.equals("")){
			Intent returnIntent= new Intent();
			returnIntent.putExtra(NEW_CAT, message);
			setResult(RESULT_OK, returnIntent);
			finish();
		}
		else{
			Intent returnIntent= new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_category, menu);
		return true;
	}

}
