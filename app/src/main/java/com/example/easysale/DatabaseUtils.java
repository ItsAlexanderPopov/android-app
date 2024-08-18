package com.example.easysale;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.room.RoomDatabase;

public class DatabaseUtils {

    private static final String TAG = "DatabaseUtils";

    // Method to print columns of a specified table
    public static void printTableColumns(Context context, String tableName) {
        UserDatabase db = UserDatabase.getDatabase(context);
        SupportSQLiteOpenHelper helper = db.getOpenHelper();
        SupportSQLiteDatabase database = helper.getWritableDatabase(); // Ensure database is writable

        // Raw SQL query to get table info
        String query = "PRAGMA table_info(" + tableName + ")";

        try (Cursor cursor = database.query(query, new String[]{})) {
            int nameIndex = cursor.getColumnIndex("name");
            int typeIndex = cursor.getColumnIndex("type");
            int pkIndex = cursor.getColumnIndex("pk");

            if (nameIndex == -1 || typeIndex == -1 || pkIndex == -1) {
                Log.e(TAG, "Column index not found");
                return;
            }

            while (cursor.moveToNext()) {
                String columnName = cursor.getString(nameIndex);
                String columnType = cursor.getString(typeIndex);
                int isPrimaryKey = cursor.getInt(pkIndex);

                Log.d(TAG, "Column Name: " + columnName);
                Log.d(TAG, "Column Type: " + columnType);
                Log.d(TAG, "Is Primary Key: " + (isPrimaryKey > 0 ? "Yes" : "No"));
                Log.d(TAG, "-----------------------------");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error printing table columns", e);
        }
    }
}
