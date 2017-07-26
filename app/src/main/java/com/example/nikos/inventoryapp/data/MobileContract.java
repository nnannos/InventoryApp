package com.example.nikos.inventoryapp.data;

/**
 * Created by NIKOS.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class MobileContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.nikos.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.nikos.inventoryapp/mobiles/ is a valid path for
     * looking at mobile data. content://com.example.nikos.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_MOBILES = "mobiles";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private MobileContract() {
    }

    /**
     * Inner class that defines constant values for the mobiles inventory database table.
     * Each entry in the table represents a single mobile.
     */
    public static class MobileEntry implements BaseColumns {
        /**
         * The content URI to access the mobile data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOBILES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of mobiles.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOBILES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single mobile.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOBILES;

        /**
         * Name of database table for mobiles
         */
        public static final String TABLE_NAME = "mobiles";
        public static final String _ID = BaseColumns._ID;

        /**
         * Model of the mobile.
         * Type: TEXT
         */
        public static final String COLUMN_MOBILE_MODEL = "model";

        /**
         * Type of the mobile OS.
         * The only possible values are {@link #TYPE_UNKNOWN}, {@link #TYPE_ANDROID},
         * Type: TEXT
         */
        public static final String COLUMN_MOBILE_TYPE = "type";
        /**
         * Mobile's price.
         * Type: INTEGER
         */
        public static final String COLUMN_MOBILE_PRICE = "price";
        /**
         * Quantity of mobiles in stock.
         * Type: INTEGER
         */
        public static final String COLUMN_MOBILE_QUANTITY = "quantity";
        /**
         * Mobile's supplier.
         * Type: INTEGER
         */
        public static final String COLUMN_MOBILE_SUPPLIER = "supplier";
        /**
         * Supplier phone number.
         * Type: String
         */
        public static final String COLUMN_MOBILE_SUPPLIER_PHONE = "telephone_supplier";
        /**
         * Supplier's email.
         * Type: String
         */
        public static final String COLUMN_MOBILE_SUPPLIER_EMAIL = "email_supplier";
        /**
         * Mobile's image.
         * Type: String
         */
        public final static String COLUMN_MOBILE_IMAGE = "image";

        /**
         * Possible values for the type of the Mobile OS.
         */
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_ANDROID = 1;

        /**
         * Returns whether or not the given type is {@link #TYPE_UNKNOWN}, or {@link #TYPE_ANDROID},
         */
        public static boolean isValidType(int type) {
            if (type == TYPE_UNKNOWN || type == TYPE_ANDROID) {
                return true;
            }
            return false;
        }
    }
}
