package com.algorhythms.infinity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TripsViewActivity extends ListActivity {
    private static final int ACTIVITY_CREATE=0;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private TripsDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trips_view_layout_2);
        mDbHelper = TripsDbAdapter.getInstance(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        setTitle("Current Trips");
    }

    private void fillData() {
        Cursor tripsCursor = mDbHelper.fetchAllTrips();
        startManagingCursor(tripsCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{TripsDbAdapter.KEY_TRIPNAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{android.R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, tripsCursor, from, to);
        setListAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if(item.getItemId()==R.id.new_trip){
    		createTrip();
    	}
    	return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteTrip(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createTrip() {
        Intent i = new Intent(this, CreateTripActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    public void createTrip(View view) {
        Intent i = new Intent(this, CreateTripActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, DrawerScreenActivity.class);
        //Intent i = new Intent(this, CreateTripActivity.class);
        i.putExtra(TripsDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
