package com.example.nikos.inventoryapp.data;

/**
 * Created by NIKOS
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nikos.inventoryapp.data.MobileContract.MobileEntry;

    /**
     * Database helper for Mobile store Inventory Tracker app. Manages database creation and version management.
     */
    public class MobileDbHelper extends SQLiteOpenHelper {
        public static final String LOG_TAG = MobileDbHelper.class.getSimpleName();

        /**
         * Name of the database file
         */
        private static final String DATABASE_NAME = "mobiles.db";

        /**
         * Database version. If you change the database schema, you must increment the database version.
         */
        private static final int DATABASE_VERSION = 1;

        /**
         * Constructs a new instance of {@link MobileDbHelper}.
         *
         * @param context of the app
         */
        public MobileDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * This is called when the database is created for the first time.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create a String that contains the SQL statement to create the mobiles table
            String SQL_CREATE_MOBILES_TABLE = "CREATE TABLE " + MobileEntry.TABLE_NAME + " ("
                    + MobileEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MobileEntry.COLUMN_MOBILE_MODEL + " TEXT NOT NULL, "
                    + MobileEntry.COLUMN_MOBILE_TYPE + " TEXT, "
                    + MobileEntry.COLUMN_MOBILE_PRICE + " INTEGER NOT NULL, "
                    + MobileEntry.COLUMN_MOBILE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                    + MobileEntry.COLUMN_MOBILE_SUPPLIER + " TEXT, "
                    + MobileEntry.COLUMN_MOBILE_SUPPLIER_PHONE + " TEXT, "
                    + MobileEntry.COLUMN_MOBILE_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                    + MobileEntry.COLUMN_MOBILE_IMAGE + " TEXT);";

            // Execute the SQL statement
            db.execSQL(SQL_CREATE_MOBILES_TABLE);
        }

        /**
         * This is called when the database needs to be upgraded.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // The database is still at version 1, so there's nothing to do be done here.
        }
    }

