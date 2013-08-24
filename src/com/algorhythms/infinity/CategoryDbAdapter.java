package com.algorhythms.infinity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A CategoryDbAdapter is an object that is used to communicate with the database in changing information that is
 * saved in the Categories table.
 * */
public class CategoryDbAdapter {

    public static final String KEY_CATNAME = "catname"; // The name of the column containing the name of each category
    public static final String KEY_ROWID = "_id"; // The name of the column containing unique id number of each category
    public static final String KEY_TRIPID = "tripid"; // The name of the column containing unique trip number this category is a part of

    private static CategoryDbAdapter instance;  // Singleton design pattern
    private static final String TAG = "CategoryDbAdapter"; // Used for debugging
    private DatabaseHelper mDbHelper; // A helper for connecting to the database
    private SQLiteDatabase mDb; // The SQLite database that is being connected to

    // If there is currently no categories table in the database, this SQL statement creates it
    public static final String DATABASE_CREATE_CATEGORIES = "CREATE TABLE categories" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, catname TEXT NOT NULL, " +
            "tripid INTEGER NOT NULL);";

    private static final String DATABASE_NAME = "data"; // The name of the database being connected to
    private static final String DATABASE_TABLE = "categories"; // The name of the table being connected to
    private static final int DATABASE_VERSION = 2; // The version of the database

    private final Context mCtx; // The context which this db adapter is working in

    // Database helper that is used to recieve information from the database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Constructor
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // There are three tables in the database, when creating it make sure all are created at the same
        // time or errors will occur
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TripsDbAdapter.DATABASE_CREATE_TRIPS);
            db.execSQL(ItemDbAdapter.DATABASE_CREATE_ITEMS);
            db.execSQL(CategoryDbAdapter.DATABASE_CREATE_CATEGORIES);
        }

        // Upgrade the category table in the database, note all category info will be lost!
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS categories");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * @param ctx the Context within which to work
     */
    private CategoryDbAdapter(Context ctx) {
        super();
        this.mCtx = ctx;
    }

    /**
     * Open the category table. If it cannot be opened, try to create a new
     * instance of the table. If it cannot be created, throw an exception to
     * signal the failure.
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public CategoryDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * @param ctx The context for the database adapter.
     * @return The singleton instance of this class.
     */
    public static CategoryDbAdapter getInstance(Context ctx) {
        if (instance == null)
            instance = new CategoryDbAdapter(ctx);

        return instance;
    }

    /**
     * Disconnect from the database
     */
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new category.
     * @param catname The name of the category.
     * @param tripid The id of the associated trip.
     * @return The row ID of the category if successful; -1 otherwise.
     */
    public long createCategory(String catname, long tripid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CATNAME, catname);
        initialValues.put(KEY_TRIPID, tripid);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the category with the given id.
     * @param rowId The id of the category to delete.
     * @return True if the deletion was successful; false otherwise.
     */
    public boolean deleteCategory(long rowId) {
        //@todo Also delete all items in the items table that belong to this category
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * @return A cursor over all categories within a particular trip.
     */
    public Cursor fetchAllCategories(long tripId) {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CATNAME, KEY_TRIPID}
                , KEY_TRIPID + "=" + tripId, null, null, null, null);
    }

    /**
     * @param rowId The id of the category to retrieve.
     * @return A cursor positioned to the matching category (if it exists).
     * @throws android.database.SQLException if the category could not be found/retrieved
     */
    public Cursor fetchCategory(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CATNAME, KEY_TRIPID},
                        KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Updates the name of the category specified by its unique rowid.
     * @param rowId The id of category to update.
     * @param catname The new name of the category.
     * @return True if the category was successfully updated; false otherwise.
     */
    public boolean updateCategory(long rowId, String catname) {
        ContentValues args = new ContentValues();
        args.put(KEY_CATNAME, catname);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}