package com.algorhythms.infinity;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DrawerScreenActivity extends Activity {

	//Copied variables from CategoryViewActivity
	//private static final int ACTIVITY_CREATE = 0;   // Custom intent identifier for creating a new activity
    //private static final int INSERT_ID = Menu.FIRST; // Index of the insert option on menu
    //private static final int DELETE_ID = Menu.FIRST + 1; // Index of the delete option on the menu
    
    /*TODO use one dbAdpater to connect to all three tables. 
     * Jamie's Explanation:
     * Remove this field from here and place into one db adapter that will be used to connect to all
     * three databases, the reason for this field is that the "id" key of all three tables have the same column
     * title, so when trying to place both a category id and a trip id in to an intent to give to an item view,
     * one is going to overwrite the other because they have the same key in the underlying map that is used to
     * pass extra information between activities through the bundles of an intent. */
    //INTERIM SOLUTION: _id will map to the trip id, catIDKey will map to the category id.
    public static final String categoryIdKey = "catIdKey";
    
    private CategoryDbAdapter mDbHelper;  // The adapter that is used for loading/changing categories in the database
    private static ItemDbAdapter mItemDbAdapter;
    private Long mTripId; // The tripId of the categories that are being viewed in the list activity
	private Long mCurCategoryId;
	//Previously existing class variables
	private static ArrayList<String> mCategories = new ArrayList<String>(Arrays.asList("Essentials", "Clothes", "Toiletries"));
	private static ArrayList<ArrayList<String>> mItems = new ArrayList<ArrayList<String>>();
	private static final int REQUEST_NEW_CAT=0;
	private static final int REQUEST_NEW_ITEM=1;
	private int mPosition;
	
	static{
		mItems.add(new ArrayList<String>(Arrays.asList("Passport", "Money", "Plane Tickets")));
		mItems.add(new ArrayList<String>(Arrays.asList("Shirts", "Pants", "Underwear")));
		mItems.add(new ArrayList<String>(Arrays.asList("Shower Gel", "Shampoo", "Conditioner")));
	}
	
	
	private ListView mDrawerList;
	private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private DrawerLayout mDrawerLayout;
	
	/*Creates and recreates the drawer list*/
	public void createDrawerList(){
		 //mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mCategories));
		fillCategories(mTripId);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_screen);
        mDbHelper = CategoryDbAdapter.getInstance(this);
        mDbHelper.open();
        mItemDbAdapter=ItemDbAdapter.getInstance(this);
        mItemDbAdapter.open();
        
        //Added Code from Old Version
        //Check the saved state for the trip id
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
        
        
       //Own Code
       //if(savedInstanceState==null) constructItemList();
        mDrawerTitle = mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.category_list);
       
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        createDrawerList();
        
        //enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
        	
        	@Override
        	public void onDrawerClosed(View view){
        		getActionBar().setTitle(mTitle);
        		invalidateOptionsMenu(); //creates call to onPrepareOptionsMenu();
        	}
        	
        	@Override
        	public void onDrawerOpened(View view){
        		getActionBar().setTitle(mDrawerTitle);
        		invalidateOptionsMenu();
        	}
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    
    
    @Override
    public void onPostCreate(Bundle savedInstanceState){
    	super.onPostCreate(savedInstanceState);
    	//Sync the toggle state
    	mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	//Pass the event to the ActionBarToggle, 
    	//if it returns true then it has handled the app icon touch event
    	if(mDrawerToggle.onOptionsItemSelected(item)){
    		return true;
    	}
    	
    	else if(item.getItemId() == (R.id.new_cat)){
    		Toast toast1=Toast.makeText(this, "New category requested", Toast.LENGTH_SHORT);
    		toast1.show();
    		createCategory();
    		/*
    		Intent categoryIntent=new Intent(this, NewCategoryActivity.class);
    		startActivityForResult(categoryIntent, REQUEST_NEW_CAT);*/
    	}
    	else if(item.getItemId()==R.id.new_item){
    		Intent itemIntent = new Intent(this, NewItemActivity.class);
    		itemIntent.putExtra(categoryIdKey, mCurCategoryId);
    		startActivityForResult(itemIntent, REQUEST_NEW_ITEM);
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode==REQUEST_NEW_CAT){
    		if(resultCode==RESULT_OK){
    			String result=data.getStringExtra(NewCategoryActivity.NEW_CAT);
    			mCategories.add(result);
    			mItems.add(new ArrayList<String>());
    			createDrawerList();
    			
    		}
    		else if (resultCode==RESULT_CANCELED){
    			Toast toast=Toast.makeText(this, "No category was created.", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    	else if(requestCode==REQUEST_NEW_ITEM){
    		if(resultCode==RESULT_OK){
    			//String itemName=data.getStringExtra(NewItemActivity.NEW_ITEM_NAME);
    			//mItems.get(mPosition).add(itemName);
    			Toast toast=Toast.makeText(this, "New item created!", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    		else if (resultCode==RESULT_CANCELED){
    			Toast toast=Toast.makeText(this, "No item was created.", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
    	@Override
    	public void onItemClick(AdapterView parent, View view, int position, long id){
    			selectItem(position, id);
    	}
    }
    
    private void selectItem(int position, long id){
    	//update the main content by replacing fragments
    	mPosition=position;
    	mCurCategoryId=id;
    	Fragment fragment = new ItemListFragment();
    	Bundle args = new Bundle();
    	args.putLong(categoryIdKey, id);
    	fragment.setArguments(args);
    	FragmentManager fragmentManager = getFragmentManager();
    	fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    	
    	//update selected drawer item and title and close drawer
    	mDrawerList.setItemChecked(position, true);
    	setTitle(getCatTitle(id));//Uses the id to get the category name. HOW DOES THIS WORK? WOW!
    	mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    /**
     * gets the category name from the database using the row id 
     * @param id
     * @return String, corresponding category name
     */
    public String getCatTitle(long id){
    	String toReturn="";
    	Cursor c = mDbHelper.fetchCategory(id);
    	toReturn=c.getString(c.getColumnIndexOrThrow(CategoryDbAdapter.KEY_CATNAME));
    	return toReturn;
    }

    @Override
    public void setTitle(CharSequence title){
    	mTitle = title;
    	getActionBar().setTitle(mTitle);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_screen_menu, menu);
        return true;
    }
    
    public static class ItemListFragment extends Fragment{
       	private Context mCtx = getActivity();
    	public ItemListFragment(){
    		//Empty Constructor
    	}
    	
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    		View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
    		long catId = getArguments().getLong(categoryIdKey);
    		Log.i("ITEMLISTFRAGMENT", "The current category id is "+catId); 		
    		ListView mItemList = (ListView) rootView.findViewById(R.id.item_list);
    		TextView emptyText = new TextView(getActivity());
    		emptyText.setText("Items will be displayed here.");
    		Cursor itemCursor=mItemDbAdapter.fetchAllItems(catId);
    		
    		if(getActivity() == null){
    			Log.i("ITEMLISTFRAGMENT", "The context is null");
    		}
    		if(itemCursor==null){
    			//emptyText.setText("No Category selected");
    			Log.i("ITEMLISTFRAGMENT", "The cursor is null");
    		}
    		else{
	        	//getActivity().startManagingCursor(itemCursor);
	        	String[] from= new String[]{ItemDbAdapter.KEY_ITEMNAME};
	        	int[] to=new int[]{android.R.id.text1};
	        	//SimpleCursorAdapter items = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, itemCursor, from, to);
	    		MyAdapter items = new MyAdapter(getActivity(), itemCursor);
	        	mItemList.setAdapter(items);
	        	
	        	//Adding a listener to the items listview
	        	
	        	mItemList.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						TextView itemInfoView = new TextView(getActivity());
						itemInfoView.setText("Item Info Will Come Here");
						PopupWindow itemPopUp = new PopupWindow(itemInfoView, 500, 500, false);
						itemPopUp.showAtLocation(view, Gravity.CENTER, 0, 0);
						
						Toast toast1=Toast.makeText(getActivity(), "Pop Up Should Have Appeared", Toast.LENGTH_SHORT);
						toast1.show();
					}
	        	});
    		}
    		
    		return rootView;
    	}
    	
    	// This is a private class that is used to update both the textview and checkbox when loading them
        // into the list view
        private class MyAdapter extends ResourceCursorAdapter {

            private Context ctx;

            public MyAdapter(Context context, Cursor cur) {
                super(context, R.layout.item_row, cur);
                ctx = context;
            }

            @Override
            public View newView(Context context, Cursor cur, ViewGroup parent) {
                LayoutInflater li = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return li.inflate(R.layout.item_row, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cur) {
                TextView tvListText = (TextView)view.findViewById(R.id.text1);
                final CheckBox cbListCheck = (CheckBox)view.findViewById(R.id.itemListCheckBox);

                final Long rowId = cur.getLong(cur.getColumnIndex(ItemDbAdapter.KEY_ROWID));
                final Context c = ctx;

                cbListCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // update the database
                        if(cbListCheck.isChecked()){
                            // Tell db this item isn't checked
                            ItemDbAdapter myItemDbAdapter = ItemDbAdapter.getInstance(ctx);
                            myItemDbAdapter.updateItem(rowId, true);
                        }
                        else
                        {
                            // Tell db this item is checked
                            ItemDbAdapter myItemDbAdapter = ItemDbAdapter.getInstance(ctx);
                            myItemDbAdapter.updateItem(rowId, false);
                        }
                    }

                });


                tvListText.setText(cur.getString(cur.getColumnIndex(ItemDbAdapter.KEY_ITEMNAME)));
                boolean b = cur.getInt(cur.getColumnIndexOrThrow(ItemDbAdapter.KEY_CHECKED)) ==0 ? false : true;
                cbListCheck.setChecked(b);
            }
    	
    	
        }
   }
    
    
    
    /**
     * Populate the list with the categories received from the database for this particular Trip.
     * @param tripId The unique id of the trip whose categories you wish to view
     */
    private void fillCategories(long tripId) {
        // Get a cursor over all categories
        Cursor catCursor = mDbHelper.fetchAllCategories(tripId);
        startManagingCursor(catCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{CategoryDbAdapter.KEY_CATNAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{android.R.id.text1};

        // Now create a simple cursor adapter and set it to display the information
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.drawer_list_item, catCursor, from, to);
        //setListAdapter(notes);
        mDrawerList.setAdapter(notes);
    }
    
      
    /**
     * Prompt the user to create a category.  This method is fired off when the create category button is pressed
     *
     * @param view The view that this method was called from
     */
    public void createCategory() {
        promptAndAddCategory();
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
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] charseq = new char[input.getText().length()];
                input.getText().getChars(0, input.getText().length(), charseq, 0);
                String catName = new String(charseq);

                if(!"".equals(catName.trim())) {
                    mDbHelper.createCategory(catName, mTripId);
                    createDrawerList();
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
