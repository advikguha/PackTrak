/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.algorhythms.infinity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Allows you to interact with the item database.
 * @author algorhythms
 */
public class ItemDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_ITEMNAME = "itemname";
    public static final String KEY_CATEGORYID = "categoryid";
    public static final String KEY_QUANTITY = "quantity";
    public static final String KEY_LOCATIONHINT = "locationhint";
    public static final String KEY_EXTRAINFO = "extrainfo";
    public static final String KEY_CHECKED = "checked";

    private static ItemDbAdapter instance;
    private static final String TAG = "ItemDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    public static final String DATABASE_CREATE_ITEMS =
            "create table items (_id integer primary key autoincrement, "
                    + "itemname text not null, categoryid integer not null, "
                    + "quantity integer not null, checked integer not null, locationhint text not null, "
                    + "extrainfo text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "items";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TripsDbAdapter.DATABASE_CREATE_TRIPS);
            db.execSQL(ItemDbAdapter.DATABASE_CREATE_ITEMS);
            db.execSQL(CategoryDbAdapter.DATABASE_CREATE_CATEGORIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS items");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    private ItemDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the items database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public ItemDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * @param ctx The context for the adapter.
     * @return The singleton instance of this class.
     */
    public static final ItemDbAdapter getInstance(Context ctx) {
        if (instance == null)
            instance = new ItemDbAdapter(ctx);
        
        return instance;
    }

    /**
     * Create a new item using the name, category, quantity, location and extra info provided. If the item is
     * successfully created return the new rowId for that item, otherwise return
     * a -1 to indicate failure.
     *
     * @param itemName the name of the item
     * @param categoryId the category it belongs to
     * @param quantity the number of items
     * @param checked default value is unchecked
     * @param locationHint a hint to its location
     * @param extraInfo any additional info
     * @return rowId or -1 if failed
     */
    public long createItem(String itemName, long categoryId, long quantity, String locationHint, String extraInfo) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ITEMNAME, itemName);
        initialValues.put(KEY_CATEGORYID, categoryId);
        initialValues.put(KEY_QUANTITY, quantity);
        initialValues.put(KEY_LOCATIONHINT, locationHint);
        initialValues.put(KEY_EXTRAINFO, extraInfo);
        initialValues.put(KEY_CHECKED, false); // when this is called, the passed parameter checked should always be 0 (False)

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the trip with the given rowId
     *
     * @param rowId id of trip to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all trips in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllItems() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ITEMNAME, KEY_CATEGORYID,
                KEY_QUANTITY, KEY_CHECKED, KEY_LOCATIONHINT, KEY_EXTRAINFO}
                , null, null, null, null, null);
    }

    /*
     * Given a category Id, get all of the items from that category
     */
    public Cursor fetchAllItems(long categoryId){
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ITEMNAME, KEY_CATEGORYID,
                KEY_QUANTITY, KEY_CHECKED, KEY_LOCATIONHINT, KEY_EXTRAINFO},
                KEY_CATEGORYID + "=" + categoryId, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the trip that matches the given rowId
     *
     * @param rowId id of trip to retrieve
     * @return Cursor positioned to matching trip, if found
     * @throws android.database.SQLException if trip could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ITEMNAME, KEY_CATEGORYID,
                        KEY_QUANTITY, KEY_CHECKED,KEY_LOCATIONHINT, KEY_EXTRAINFO}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the trip using the details provided. The trip to be updated is
     * specified using the rowId, and it is altered to use the new values
     * passed in title and body
     *
     * @param rowId id of trip to update
     * @param itemName value to set trip name to
     * @param quantity The quantity of the item.
     * @param checked True if the item has been checked off; false otherwise.
     * @param locationHint A hint as to where the item is located.
     * @return true if the trip was successfully updated, false otherwise
     */
    public boolean updateItem(long rowId, String itemName, long quantity, boolean checked,
                                String locationHint, String extraInfo) {

        ContentValues args = new ContentValues();
        args.put(KEY_ITEMNAME, itemName);
        args.put(KEY_QUANTITY, quantity);
        args.put(KEY_CHECKED, checked);
        args.put(KEY_LOCATIONHINT, locationHint);
        args.put(KEY_EXTRAINFO, extraInfo);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /*
     *  Update the database, this item has been checked
     */
    public void updateItem(long rowId, boolean checked){
        ContentValues args = new ContentValues();
        args.put(KEY_CHECKED, checked);
        mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }
}
