package com.example.nikos.inventoryapp;

/**
 * Created by NIKOS.
 */

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nikos.inventoryapp.data.MobileContract.MobileEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = com.example.nikos.inventoryapp.EditorActivity.class.getSimpleName();

    private static final int MOBILE_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    MobileCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, com.example.nikos.inventoryapp.EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView mobileListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        mobileListView.setEmptyView(emptyView);

        mCursorAdapter = new MobileCursorAdapter(this, null);
        mobileListView.setAdapter(mCursorAdapter);

        // Set a clickListener on that View
        mobileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create a new intent to open the {@link FamilyActivity}
                Intent intent = new Intent(CatalogActivity.this, com.example.nikos.inventoryapp.EditorActivity.class);
                Uri currentMobileUri = ContentUris.withAppendedId(MobileEntry.CONTENT_URI, id);
                intent.setData(currentMobileUri);
                //Start the new activity
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(MOBILE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MobileEntry._ID,
                MobileEntry.COLUMN_MOBILE_MODEL,
                MobileEntry.COLUMN_MOBILE_TYPE,
                MobileEntry.COLUMN_MOBILE_PRICE,
                MobileEntry.COLUMN_MOBILE_QUANTITY,
                MobileEntry.COLUMN_MOBILE_SUPPLIER,
                MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE,
                MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL,
                MobileEntry.COLUMN_MOBILE_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                MobileEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link MobileCursorAdapter} with this new cursor containing updated mobile data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_data:
                insertMobiles();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to insert hardcoded mobiles data into the database.
     */
    private void insertMobiles() {
        insertMobile("MLS k2", 1, 70, 10, "MLS Trend", "+24 3232", "mobiles@MLS.gr", R.drawable.tel1);
        insertMobile("TEL K124", 1, 120, 15, "TEL Corp", "+30 210 0000000", "technology@TEL.gr", R.drawable.tel2);
        insertMobile("GOOGLE 5.1", 1, 650, 5, "eshop.gr", "+30 5555 000000", "tech@gmail.com", R.drawable.tel3);
        insertMobile("TELEM H1", 0, 40, 3, "eshop.gr", "", "mail@eshop.gr", R.drawable.tel3);
        insertMobile("TELEF J3", 0, 45, 5, "Eshop.gr", "+30 2230 000000", "techn@TELEF.gr", R.drawable.tel5);
    }

    private void insertMobile(String model, int type, int price, int quantity, String supplier, String supplierPhone, String supplierEmail, int imageId) {
        // Create a ContentValues object where column names are the keys,
        // and mobile's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(MobileEntry.COLUMN_MOBILE_MODEL, model);
        values.put(MobileEntry.COLUMN_MOBILE_TYPE, type);
        values.put(MobileEntry.COLUMN_MOBILE_PRICE, price);
        values.put(MobileEntry.COLUMN_MOBILE_QUANTITY, quantity);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER, supplier);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE, supplierPhone);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL, supplierEmail);

        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(imageId)
                + '/' + getResources().getResourceTypeName(imageId) + '/' + getResources().getResourceEntryName(imageId));

        values.put(MobileEntry.COLUMN_MOBILE_IMAGE, imageUri.toString());

        // Use the {@link MobileEntry#CONTENT_URI} to indicate that we want to insert
        // into the mobiles database table.
        // Receive the new content URI that will allow us to access database's data in the future.
        Uri newUri = getContentResolver().insert(MobileEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_mobile_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_mobile_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete everything
     */
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                int rowsDeleted = getContentResolver().delete(MobileEntry.CONTENT_URI, null, null);
                Log.v("CatalogActivity", rowsDeleted + " rows deleted from mobiles database");
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}