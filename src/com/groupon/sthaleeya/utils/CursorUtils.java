package com.groupon.sthaleeya.utils;

import android.database.Cursor;
import android.util.Log;

/**
 * Convenient functions for dealing with Cursors.
 */
public class CursorUtils {
    private static final String TAG = "CursorUtils";

    /**
     * Gets a string from the cursor by column name.
     * @param columnName Column name.
     * @param cursor Cursor.
     * @return String value or null.
     */
    public static String getStringFromCursor(String columnName, Cursor cursor) {
        String result = null;
        try {
            int columnIndex = (cursor == null) ? -1 : cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                result = cursor.getString(columnIndex);
            }
        } catch (RuntimeException ex) {
            Log.w(TAG, "getStringFromCursor() failed", ex);
        }

        return result;
    }

    /**
     * Gets an Integer from the cursor by column name.
     * @param columnName Column name.
     * @param cursor Cursor.
     * @return Integer value or null.
     */
    public static Integer getIntegerFromCursor(String columnName, Cursor cursor) {
        Integer result = null;
        try {
            int columnIndex = (cursor == null) ? -1 : cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                result = cursor.getInt(columnIndex);
            }
        } catch (RuntimeException ex) {
            Log.w(TAG, "getIntegerFromCursor() failed", ex);
        }

        return result;
    }

    /**
     * Gets an Long from the cursor by column name.
     * @param columnName Column name.
     * @param cursor Cursor.
     * @return Long value or null.
     */
    public static Long getLongFromCursor(String columnName, Cursor cursor) {
        Long result = null;
        try {
            int columnIndex = (cursor == null) ? -1 : cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                result = cursor.getLong(columnIndex);
            }
        } catch (RuntimeException ex) {
            Log.w(TAG, "getLongFromCursor() failed", ex);
        }

        return result;
    }

    /**
     * Gets a double value from the cursor by column name.
     * @param columnName Column name.
     * @param cursor Cursor.
     * @return Double value or null.
     */
    public static Double getDoubleFromCursor(String columnName, Cursor cursor) {
        Double result = null;
        try {
            int columnIndex = (cursor == null) ? -1 : cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                result = cursor.getDouble(columnIndex);
            }
        } catch (RuntimeException ex) {
            Log.w(TAG, "getLongFromCursor() failed", ex);
        }

        return result;
    }

    /**
     * Helper method to safely close a cursor. Any exceptions while closing
     * will be ignored.
     *
     * @param cursor Cursor to be closed. May be null.
     */
    public static void safeClose(Cursor cursor) {
        try {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
                    Log.d(TAG, "Cursor closed");
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Ignoring exception on cursor close", e);
        }
    }
}