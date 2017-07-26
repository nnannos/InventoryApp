package com.example.nikos.inventoryapp.data;

/**
 * Created by NIKOS
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nikos.inventoryapp.data.MobileContract.MobileEntry;

/**
 * {@link ContentProvider} for Mobile store Inventory Tracker app.
 */
public class MobileProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MobileProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the Mobiles table
     */
    private static final int MOBILES = 100;
    /**
     * URI matcher code for the content URI for a single Mobile in the Mobiles table
     */
    private static final int MOBILE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(MobileContract.CONTENT_AUTHORITY, MobileContract.PATH_MOBILES, MOBILES);
        sUriMatcher.addURI(MobileContract.CONTENT_AUTHORITY, MobileContract.PATH_MOBILES + "/#", MOBILE_ID);
    }

    private MobileDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new MobileDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOBILES:
                // For the MOBILES code, query the mobiless table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the mobiless table.

                cursor = database.query(MobileEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MOBILE_ID:
                // For the MOBILE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.nikos.mobiles/mobiles/4",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 4 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MobileEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the mobiles table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MobileEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOBILES:
                return MobileEntry.CONTENT_LIST_TYPE;
            case MOBILE_ID:
                return MobileEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOBILES:
                return insertMobile(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOBILES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(MobileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOBILE_ID:
                // Delete a single row given by the ID in the URI
                selection = MobileEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MobileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOBILES:
                return updateMobile(uri, contentValues, selection, selectionArgs);
            case MOBILE_ID:
                // For the MOBILE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MobileEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMobile(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Insert a mobile into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertMobile(Uri uri, ContentValues values) {

        // Check that the title is not null
        String name = values.getAsString(MobileEntry.COLUMN_MOBILE_MODEL);
        if (name == null) {
            throw new IllegalArgumentException("Mobile requires a model");
        }


        // Check that the type is valid
        Integer type = values.getAsInteger(MobileEntry.COLUMN_MOBILE_TYPE);
        if (type == null || !MobileEntry.isValidType(type)) {
            throw new IllegalArgumentException("Mobile requires valid type");
        }

        // Check that the price is greater than or equal to 0
        Integer price = values.getAsInteger(MobileEntry.COLUMN_MOBILE_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Mobile requires a price");
        } else if (price < 0) {
            throw new IllegalArgumentException("Mobile requires valid price");
        }

        // Check that the quantity is greater than or equal to 0
        Integer quantity = values.getAsInteger(MobileEntry.COLUMN_MOBILE_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Mobile requires a quantity");
        } else if (quantity < 0) {
            throw new IllegalArgumentException("Mobile requires valid quantity");
        }

        // No need to check the supplier, any value is valid (including null).

        // Check that the supplier's email is not null
        String supplierEmail = values.getAsString(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Mobile requires a supplier's email");
        }


        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new mobile with the given values
        long id = database.insert(MobileEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the mobile content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update mobiles in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more mobiles).
     * Return the number of rows that were successfully updated.
     */
    private int updateMobile(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link MobileEntry#COLUMN_MOBILE_MODEL} key is present,
        // check that the title value is not null.
        if (values.containsKey(MobileEntry.COLUMN_MOBILE_MODEL)) {
            String title = values.getAsString(MobileEntry.COLUMN_MOBILE_MODEL);
            if (title == null) {
                throw new IllegalArgumentException("Mobile requires a title");
            }
        }


        // If the {@link MobileEntry#COLUMN_MOBILE_TYPE} key is present,
        // check that the type value is valid.
        if (values.containsKey(MobileEntry.COLUMN_MOBILE_TYPE)) {
            Integer type = values.getAsInteger(MobileEntry.COLUMN_MOBILE_TYPE);
            if (type == null || !MobileEntry.isValidType(type)) {
                throw new IllegalArgumentException("Mobile requires valid type");
            }
        }

        // If the {@link MobileEntry#COLUMN_MOBILE_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(MobileEntry.COLUMN_MOBILE_PRICE)) {
            // Check that the price is greater than or equal to 0
            Integer price = values.getAsInteger(MobileEntry.COLUMN_MOBILE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Mobile requires valid price");
            }
        }

        // If the {@link MobileEntry#COLUMN_MOBILE_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(MobileEntry.COLUMN_MOBILE_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(MobileEntry.COLUMN_MOBILE_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Mobile requires valid quantity");
            }
        }

        // No need to check the supplier, any value is valid (including null).

        // If the {@link MobileEntry#COLUMN_MOBILE_SUPPLIER_EMAIL} key is present,
        // check that the email_supplier value is not null.
        if (values.containsKey(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Mobile requires a supplier's email for orders");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MobileEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}