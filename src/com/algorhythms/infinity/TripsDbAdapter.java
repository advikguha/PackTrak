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


public class TripsDbAdapter {

    public static final String KEY_TRIPNAME = "tripname";
    public static final String KEY_STARTDATE = "startdate";
    public static final String KEY_ENDDATE = "enddate";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TRIPTYPE =  "triptype";

    private static TripsDbAdapter instance;
    private static final String TAG = "TripsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    public static final String DATABASE_CREATE_TRIPS =
        "create table trips (_id integer primary key autoincrement, "
        + "tripname text not null, startdate text not null, enddate text not null, triptype text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "trips";
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
            db.execSQL("DROP TABLE IF EXISTS trips");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    private TripsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the trips database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public TripsDbAdapter open() throws SQLException {
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
    public static final TripsDbAdapter getInstance(Context ctx) {
        if (instance == null)
            instance = new TripsDbAdapter(ctx);

        return instance;
    }

    /**
     * Create a new trip using the name, start and end dates, and trip type provided. If the trip is
     * successfully created return the new rowId for that trip, otherwise return
     * a -1 to indicate failure.
     * 
     * @param tripName the name of the trip
     * @param startDate the date the trip begins
     * @param endDate the date the trip ends
     * @param tripType the type of trip this is
     * @return rowId or -1 if failed
     */
    public long createTrip(String tripName, String startDate, String endDate, String tripType) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRIPNAME, tripName);
        initialValues.put(KEY_STARTDATE, startDate);
        initialValues.put(KEY_ENDDATE, endDate);
        initialValues.put(KEY_TRIPTYPE, tripType);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the trip with the given rowId
     * 
     * @param rowId id of trip to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteTrip(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all trips in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllTrips() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TRIPNAME,
                KEY_STARTDATE, KEY_ENDDATE, KEY_TRIPTYPE}
                , null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the trip that matches the given rowId
     * 
     * @param rowId id of trip to retrieve
     * @return Cursor positioned to matching trip, if found
     * @throws android.database.SQLException if trip could not be found/retrieved
     */
    public Cursor fetchTrip(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TRIPNAME,
                    KEY_STARTDATE, KEY_ENDDATE, KEY_TRIPTYPE}, KEY_ROWID + "=" + rowId, null,
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
     * @param tripName value to set trip name to
     * @param startDate value to set trip startDate to
     * @param endDate value to set trip endDate to
     * @param tripType value to set trip type to
     * @return true if the trip was successfully updated, false otherwise
     */
    public boolean updateTrip(long rowId, String tripName, String startDate, String endDate, String tripType) {
        ContentValues args = new ContentValues();
        args.put(KEY_TRIPNAME, tripName);
        args.put(KEY_STARTDATE, startDate);
        args.put(KEY_ENDDATE, endDate);
        args.put(KEY_TRIPTYPE, tripType);


        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
