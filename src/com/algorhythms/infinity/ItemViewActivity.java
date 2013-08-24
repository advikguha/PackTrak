package com.algorhythms.infinity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ItemViewActivity extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private ItemDbAdapter mDbHelper;

    private Long mCatId;

    // This is a private class that is used to update both the textview and text view when loading them
    // into the list view
    private class MyAdapter extends ResourceCursorAdapter {

        private Context ctx;

        public MyAdapter(Context context, Cursor cur) {
            super(context, R.layout.item_row, cur);
            ctx = context;
        }

        @Override
        public View newView(Context context, Cursor cur, ViewGroup parent) {
            LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_view_layout);

        // Check the extras for the category id
        Bundle extras = getIntent().getExtras();
        mCatId = extras != null ? extras.getLong(CategoryViewActivity.categoryIdKey) : null;

        // If at this point there is no category id, glitch has occured, finish this activity
        if(mCatId == null)
            finish();

        // Get the trips adapter and use it to access the title of this trip
        CategoryDbAdapter categoriesAdapter = CategoryDbAdapter.getInstance(this);
        categoriesAdapter.open();

        Cursor item = categoriesAdapter.fetchCategory(mCatId);
        startManagingCursor(item);
        TextView title = (TextView)findViewById(R.id.itemsHeader);
        title.setText("Items for " + item.getString(item.getColumnIndexOrThrow(CategoryDbAdapter.KEY_CATNAME)));

        mDbHelper = ItemDbAdapter.getInstance(this);
        mDbHelper.open();
        fillData(mCatId);
        registerForContextMenu(getListView());
    }

    private void fillData(long mCatId) {
        Cursor itemsCursor = mDbHelper.fetchAllItems(mCatId);
        startManagingCursor(itemsCursor);

        MyAdapter mListAdapter = new MyAdapter(this, itemsCursor);
        setListAdapter(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_add);
        return true;
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
                mDbHelper.deleteItem(info.id);
                fillData(mCatId);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createItem(View view) {
        Intent i = new Intent(this, CreateItemActivity.class);
        i.putExtra(CategoryViewActivity.categoryIdKey, mCatId);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, CreateItemActivity.class);
        i.putExtra(ItemDbAdapter.KEY_ROWID, id);
        i.putExtra(CategoryViewActivity.categoryIdKey, mCatId);
        startActivityForResult(i, ACTIVITY_EDIT);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData(mCatId);
    }

}