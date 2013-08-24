package com.algorhythms.infinity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  This ListActivity is used to display all of the categories for a given trip to the user in a nice list format.
 */
public class CategoryViewActivity extends ListActivity {
    private static final int ACTIVITY_CREATE = 0;   // Custom intent identifier for creating a new activity

    private static final int INSERT_ID = Menu.FIRST; // Index of the insert option on menu
    private static final int DELETE_ID = Menu.FIRST + 1; // Intex of the delete option on the menu

    /**
     * @todo Read below explanation
     * Remove this field from here and place into one db adapter that will be used to connect to all
     * three databases, the reason for this field is that the "id" key of all three tables have the same column
     * title, so when trying to place both a category id and a trip id in to an intent to give to an item view,
     * one is going to overwrite the other because they have the same key in the underlying map that is used to
     * pass extra information between activies through the bundles of an intent.
     */
    public static final String categoryIdKey = "catIdKey";

    private CategoryDbAdapter mDbHelper;  // The adapter that is used for loading/changing categories in the database
    private Long mTripId; // The tripId of the categories that are being viewed in the list activity

    /**
     *   Initialize this activity with its layout, and attempt to read the tripId of the categories that should
     *   be getting loaded into it from both the Bundle and from the savedInstance state.
     *
     *   @param savedInstanceState A bundle used to transfer the trip id of the caregories that should be getting displayed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view
        setContentView(R.layout.category_view_layout);

        // Check the saved state for the trip id
        mTripId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(TripsDbAdapter.KEY_ROWID);
        // Check the bundle received from the previous activity for the trip id
        if (mTripId == null) {
            Bundle extras = getIntent().getExtras();
            mTripId = extras != null ? extras.getLong(TripsDbAdapter.KEY_ROWID)
                    : null;
        }
        // If at this point there is no trip id, error has occurred, finish this activity
        if(mTripId == null)
            finish();

        // Get the trips adapter and use it to access the title of this trip
        TripsDbAdapter tripsAdapter = TripsDbAdapter.getInstance(this);
        tripsAdapter.open();

        // Using the trips adapter, get the trip these categories belong to and change the header of the content
        // view to the name of this trip.
        Cursor item = tripsAdapter.fetchTrip(mTripId);
        startManagingCursor(item);
        TextView title = (TextView)findViewById(R.id.categoryHeader);
        title.setText("Categories for " + item.getString(item.getColumnIndexOrThrow(TripsDbAdapter.KEY_TRIPNAME)));

        // Get the categories adapter and fill in all of the categories for this trip
        mDbHelper = CategoryDbAdapter.getInstance(this);
        mDbHelper.open();
        fillData(mTripId);
        registerForContextMenu(getListView());
    }

    /**
     * Populate the list with the categories received from the database for this particular Trip.
     * @param tripId The unique id of the trip whose categories you wish to view
     */
    private void fillData(long tripId) {
        // Get a cursor over all categories
        Cursor catCursor = mDbHelper.fetchAllCategories(tripId);
        startManagingCursor(catCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{CategoryDbAdapter.KEY_CATNAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display the information
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.list_row, catCursor, from, to);
        setListAdapter(notes);
    }

    /**
     *  @todo I do not think this method is required because we are hiding the options menu, look into this.
     *
     * When the options menu is created, add a menu item to it that allows you to add new categories?
     *
     * @param menu The menu in which you want to add a metnu item to
     * @return The options menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_add);
        return true;
    }

    /**
     * @todo I think this is the code that makes a delete option pop up when holding down on a category, look into this.
     *
     * When the context menu is created, add a menu item to it that allows you to delete categories?
     *
     * @param menu  The context menu that was created?
     * @param v     The view this context menu is for?
     * @param menuInfo  Info about this context menu?
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    /**
     * @todo I think this is the actual method that is fired off when clicking delete after holding down on a category, look into this
     *
     * Delete the category whose information is being displayed in the menu item whose delete option was clicked.
     *
     * @param item The Menu item that was selected
     * @return ??
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteCategory(info.id);
                fillData(mTripId);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Prompt the user to create a category.  This method is fired off when the create category button is pressed
     *
     * @param view The view that this method was called from
     */
    public void createCategory(View view) {
        promptAndAddCategory();
    }

    /**
     * This method is fired off when clicking on one of the categories in the list view.  This will take you to an
     * activity that displays all of the items for the given activity.
     *
     * @param l The list view whose list item was clicked on
     * @param v The view that the list view was contained in?
     * @param position  The poisiton in the list view that was clicked?
     * @param id  The id of the
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ItemViewActivity.class);
        i.putExtra(categoryIdKey, id);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Prompt the user to create a category, ask them for the name of the category they want to create.
     */
    private void promptAndAddCategory() {
        // Create the alert (popup box) that the user will use to create the category
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add Category");
        alert.setMessage("What's the name of this category?");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        final Context c = this;

        // Create the positive button and attach a click listner to it which will create a category
        // if the category name is a valid one
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] charseq = new char[input.getText().length()];
                input.getText().getChars(0, input.getText().length(), charseq, 0);
                String catName = new String(charseq);

                if(!"".equals(catName.trim())) {
                    mDbHelper.createCategory(catName, mTripId);
                    fillData(mTripId);
                }
            }
        });

        // Create the negative button, just closes the alert if clicked
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        // Display the alert
        alert.show();
    }
}