package com.example.nikos.inventoryapp;

/**
 * Created by NIKOS.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikos.inventoryapp.data.MobileContract.MobileEntry;
import com.example.nikos.inventoryapp.data.MobileContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Integer.parseInt;


/**
 * Allows user to add a new mobile to the store or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    // The request code to store image from the Gallery
    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Identifier for the mobile data loader
     */
    private static final int EXISTING_MOBILE_LOADER = 0;

    /**
     * EditText field to enter the mobile's model
     */
    @BindView(R.id.edit_mobile_model)
    EditText mModelEditText;

    /**
     * Spinner field to enter the mobile's type
     */
    @BindView(R.id.spinner_type)
    Spinner mTypeSpinner;
    /**
     * EditText field to enter the mobile's price
     */
    @BindView(R.id.edit_mobile_price)
    EditText mPriceEditText;
    /**
     * EditText field to enter the mobile's quantity
     */
    @BindView(R.id.edit_mobile_quantity)
    EditText mQuantityEditText;
    /**
     * Button field to decrease mobile's quantity
     */
    @BindView(R.id.button_minus)
    Button mMinusButton;
    /**
     * Button field to increase mobile's quantity
     */
    @BindView(R.id.button_plus)
    Button mPlusButton;
    /**
     * EditText field to enter the mobile's supplier
     */
    @BindView(R.id.edit_supplier)
    EditText mSupplierEditText;
    /**
     * EditText field to enter the supplier's telephone
     */
    @BindView(R.id.edit_supplier_phone)
    EditText mSupplierPhoneEditText;
    /**
     * EditText field to enter the supplier's email
     */
    @BindView(R.id.edit_supplier_email)
    EditText mSupplierEmailEditText;
    /**
     * ImageView field to control mobile image
     */
    @BindView(R.id.edit_mobile_image)
    ImageView mImageView;
    @BindView(R.id.order_now_textview)
    TextView mOrderNowTextView;
    /**
     * Button field to make a phone call to supplier
     */
    @BindView(R.id.button_telephone)
    ImageButton mSupplierPhoneButton;
    /**
     * Button field to compose an email
     */
    @BindView(R.id.button_email)
    ImageButton mSupplierEmailButton;
    /**
     * Button field to delete the mobile from database
     */
    @BindView(R.id.button_delete)
    Button mDeleteButton;
    private Uri mImageUri;
    /**
     * Content URI for the existing mobile (null if it's a new mobile)
     */
    private Uri mCurrentMobileUri;
    /**
     * Type of mobile OS. The possible valid values are in the MobileContract.java file:
     * {@link MobileEntry#TYPE_UNKNOWN}, or  * {@link MobileEntry#TYPE_ANDROID}.
     */
    private int mType = MobileContract.MobileEntry.TYPE_UNKNOWN;

    /**
     * Boolean flag that keeps track of whether the mobile has been edited (true) or not (false)
     */
    private boolean mMobileHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mMobileHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMobileHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new mobile or editing an existing one.
        Intent intent = getIntent();
        mCurrentMobileUri = intent.getData();

        // If the intent DOES NOT contain a mobile content URI, then we know that we are
        // creating a new mobile.
        if (mCurrentMobileUri == null) {
            // This is a new mobile, so change the app bar to say "Add a mobile"
            setTitle(getString(R.string.editor_activity_model_new_mobile));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a mobile that hasn't been created yet.)
            invalidateOptionsMenu();

            mOrderNowTextView.setVisibility(View.GONE);
            mSupplierPhoneButton.setVisibility(View.GONE);
            mSupplierEmailButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility((View.GONE));
        } else {
            // Otherwise this is an existing mobile, so change app bar to say "Edit Mobile"
            setTitle(getString(R.string.editor_activity_model_edit_mobile));

            // Initialize a loader to read the mobile data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_MOBILE_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mModelEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mDeleteButton.setOnTouchListener(mTouchListener);
        setupSpinner();

        // Set a clickListener on minus button
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    int quantity = parseInt(mQuantityEditText.getText().toString().trim());
                    if (quantity == 0) {
                        quantity = 0;
                        Toast.makeText(view.getContext(), getString(R.string.negative_quantity_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        quantity--;
                    }
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });

        // Set a clickListener on plus button
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    quantity = parseInt(mQuantityEditText.getText().toString().trim());
                    quantity++;
                } else {
                    quantity = 1;
                }
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        // Set a clickListener on telephone button
        mSupplierPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString()));
                startActivity(intent);
            }
        });

        // Set a clickListener on email button
        mSupplierEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mSupplierEmailEditText.getText().toString().trim())); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "Order more from mobile: " + mModelEditText.getText().toString().trim());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Set a clickListener on delete button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    /**
     * Get user input from editor and save mobile into database.
     */
    private boolean saveMobile() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String modelString = mModelEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();


        // Check if this is supposed to be a new mobile
        // and check if all the fields in the editor are blank
        if (mCurrentMobileUri == null && mImageUri == null &&
                TextUtils.isEmpty(modelString) &&
                mType == MobileEntry.TYPE_UNKNOWN && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(supplierPhoneString) && TextUtils.isEmpty(supplierEmailString)) {
            // Since no fields were modified, we can return early without creating a new mobile.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return true;
        } else if (TextUtils.isEmpty(modelString)) {
            Toast.makeText(this, R.string.no_model_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString) || parseInt(priceString) <= 0) {
            Toast.makeText(this, R.string.no_price_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, R.string.no_email_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mImageUri == null) {
            Toast.makeText(this, R.string.no_image_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and mobile attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(MobileEntry.COLUMN_MOBILE_MODEL, modelString);
        values.put(MobileEntry.COLUMN_MOBILE_TYPE, mType);
        values.put(MobileEntry.COLUMN_MOBILE_PRICE, priceString);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = parseInt(quantityString);
        }
        values.put(MobileEntry.COLUMN_MOBILE_QUANTITY, quantity);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER, supplierString);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE, supplierPhoneString);
        values.put(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL, supplierEmailString);

        if (mImageUri == null) {
            mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.no_image)
                    + '/' + getResources().getResourceTypeName(R.drawable.no_image) + '/' + getResources().getResourceEntryName(R.drawable.no_image));
        }

        values.put(MobileEntry.COLUMN_MOBILE_IMAGE, mImageUri.toString());

        // Determine if this is a new or existing mobile by checking if mCurrentMobileUri is null or not
        if (mCurrentMobileUri == null) {
            // This is a NEW mobile, so insert a new mobile into the provider,
            // returning the content URI for the new mobile.
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
        } else {
            // Otherwise this is an EXISTING mobile, so update the mobile with content URI: mCurrentMobileUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentMobileUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMobileUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_mobile_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_mobile_successful),
                        Toast.LENGTH_SHORT).show();

            }
        }

        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the mobile.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_android))) {
                        mType = MobileEntry.TYPE_ANDROID;
                    } else {
                        mType = MobileEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = MobileEntry.TYPE_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new mobile, hide the "Delete" menu item.
        if (mCurrentMobileUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save mobile to database and exit activity - only if the required fields are filled
                if (saveMobile()) finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the mobile hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mMobileHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the mobile hasn't changed, continue with handling back button press
        if (!mMobileHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all mobile attributes, define a projection that contains
        // all columns from the mobile table
        String[] projection = {
                MobileEntry._ID,
                MobileEntry.COLUMN_MOBILE_MODEL,
                MobileEntry.COLUMN_MOBILE_QUANTITY,
                MobileEntry.COLUMN_MOBILE_PRICE,
                MobileEntry.COLUMN_MOBILE_TYPE,
                MobileEntry.COLUMN_MOBILE_SUPPLIER,
                MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE,
                MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL,
                MobileEntry.COLUMN_MOBILE_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentMobileUri,         // Query the content URI for the current mobile
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of mobile attributes that we're interested in
            int modelColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_MODEL);
            int priceColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_PRICE);
            int typeColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_TYPE);
            int quantityColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(MobileEntry.COLUMN_MOBILE_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String model = cursor.getString(modelColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierTelephone = cursor.getString(supplierPhoneColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mModelEditText.setText(model);

            // Type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Android).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (type) {
                case MobileEntry.TYPE_ANDROID:
                    mTypeSpinner.setSelection(1);
                    break;
                default:
                    mTypeSpinner.setSelection(0);
                    break;
            }
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mSupplierPhoneEditText.setText(supplierTelephone);
            mSupplierEmailEditText.setText(supplierEmail);
            mImageUri = Uri.parse(image);
            mImageView.setImageURI(mImageUri);

            if (TextUtils.isEmpty(mSupplierPhoneEditText.getText()))
                mSupplierPhoneButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mModelEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mTypeSpinner.setSelection(0); // Select "Unknown" type
        mImageUri = null;
        mSupplierEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mSupplierEmailEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the mobile.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this mobile
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the mobile.
                deleteMobile();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the mobile.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the mobile in the database.
     */
    private void deleteMobile() {
        // Only perform the delete if this is an existing mobile.
        if (mCurrentMobileUri != null) {
            // Call the ContentResolver to delete the mobile at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMobileUri
            // content URI already identifies the mobile that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentMobileUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_mobile_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_mobile_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                //mImageView.setText(mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}