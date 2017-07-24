package me.samuki.remainder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ManagerDbAdapter {
    public static final String DEBUG_TAG = "SqLiteManager";

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "database.db";
    public static final String DB_ACTION_TABLE = "actions";
    public static final String DB_TMP_TABLE = "tmp";

    public static final String KEY_ID = "_id";
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;
    public static final String KEY_ACTIVE = "active";
    public static final String ACTIVE_OPTIONS = "INTEGER DEFAULT 1";
    public static final int ACTIVE_COLUMN = 1;
    public static final String KEY_ACTION_NAME = "action_name";
    public static final String ACTION_NAME_OPTIONS = "TEXT NOT NULL";
    public static final int ACTION_NAME_COLUMN = 2;
    public static final String KEY_TYPE = "type";
    public static final String TYPE_OPTIONS = "TEXT NOT NULL";
    public static final int TYPE_COLUMN = 3;
    public static final String KEY_AMOUNT = "amount";
    public static final String AMOUNT_OPTIONS = "INTEGER NOT NULL";
    public static final int AMOUNT_COLUMN = 4;
    public static final String KEY_DATE = "to_date";
    public static final String DATE_OPTIONS = "DATE DEFAULT 0";
    public static final int DATE_COLUMN = 5;
    public static final String KEY_REPEAT = "repeats";
    public static final String REPEAT_OPTIONS = "INTEGER DEFAULT 0";
    public static final int REPEAT_COLUMN = 6;
    public static final String KEY_OFTEN = "how_often";
    public static final String OFTEN_OPTIONS = "TEXT DEFAULT \'default\'";
    public static final int OFTEN_COLUMN = 7;
    public static final String KEY_TO_BE_DONE = "to_be_done";
    public static final String TO_BE_DONE_OPTIONS = "INTEGER NOT NULL";
    public static final int TO_BE_DONE_COLUMN = 8;


    public static final String CREATE_ACTION_TABLE =
            "CREATE TABLE " + DB_ACTION_TABLE + "(" + KEY_ID + " " + ID_OPTIONS +", " +
                                            KEY_ACTIVE +" " + ACTIVE_OPTIONS +", "+
                                            KEY_ACTION_NAME + " " + ACTION_NAME_OPTIONS + ", " +
                                            KEY_TYPE + " " + TYPE_OPTIONS +", " +
                                            KEY_AMOUNT +" " + AMOUNT_OPTIONS + ", " +
                                            KEY_DATE + " " + DATE_OPTIONS + ", " +
                                            KEY_REPEAT + " " + REPEAT_OPTIONS + ", " +
                                            KEY_OFTEN + " " + OFTEN_OPTIONS  + ", " +
                                            KEY_TO_BE_DONE + " " + TO_BE_DONE_OPTIONS + " );";
    public static final String DROP_ACTION_TABLE =
            "DROP TABLE IF EXISTS " + DB_ACTION_TABLE;
    public static final String INSERT_INTO_ACTION_TABLE =
            "INSERT INTO " + DB_ACTION_TABLE + " SELECT * FROM  " + DB_TMP_TABLE;
    //TMP TABLE FOR UPDATING
    public static final String CREATE_TMP_TABLE =
            "CREATE TABLE tmp(" + KEY_ID + " " + ID_OPTIONS +", " +
                    KEY_ACTIVE +" " + ACTIVE_OPTIONS +", "+
                    KEY_ACTION_NAME + " " + ACTION_NAME_OPTIONS + ", " +
                    KEY_TYPE + " " + TYPE_OPTIONS +", " +
                    KEY_AMOUNT +" " + AMOUNT_OPTIONS + ", " +
                    KEY_DATE + " " + DATE_OPTIONS + ", " +
                    KEY_REPEAT + " " + REPEAT_OPTIONS + ", " +
                    KEY_OFTEN + " " + OFTEN_OPTIONS  + ", " +
                    KEY_TO_BE_DONE + " " + TO_BE_DONE_OPTIONS + " );";
    public static final String DROP_TMP_TABLE =
            "DROP TABLE IF EXISTS tmp";
    public static final String INSERT_INTO_TMP_TABLE =
            "INSERT INTO " + DB_TMP_TABLE + " SELECT * FROM " + DB_ACTION_TABLE;

    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int verion) {
            super(context, name, factory, verion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_ACTION_TABLE);

            Log.d(DEBUG_TAG, "Database Creating...");
            Log.d(DEBUG_TAG, "Table " + DB_ACTION_TABLE +" version " + DB_VERSION +" created!");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(CREATE_TMP_TABLE);
            db.execSQL(INSERT_INTO_TMP_TABLE);

            db.execSQL(DROP_ACTION_TABLE);

            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + DB_ACTION_TABLE + " updated from version" + oldVersion +
                    " to version" + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");

            onCreate(db);
            db.execSQL(INSERT_INTO_ACTION_TABLE);
            db.execSQL(DROP_TMP_TABLE);
        }
    }

    public ManagerDbAdapter(Context context) {
        this.context = context;
    }

    public ManagerDbAdapter open() {
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertAction(String name, String type, long amount, String date) {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_ACTION_NAME, name);
        newValue.put(KEY_TYPE, type);
        newValue.put(KEY_AMOUNT, amount);
        newValue.put(KEY_DATE, date);
        if(type.equals(context.getString(R.string.time)))
            newValue.put(KEY_TO_BE_DONE, amount);
        else
            newValue.put(KEY_TO_BE_DONE, 0);
        return db.insert(DB_ACTION_TABLE, null, newValue);
    }

    public long insertAction(String name, String type, long amount, String date, int repeat, String often) {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_ACTION_NAME, name);
        newValue.put(KEY_TYPE, type);
        newValue.put(KEY_AMOUNT, amount);
        newValue.put(KEY_DATE, date);
        newValue.put(KEY_REPEAT, repeat);
        newValue.put(KEY_OFTEN, often);
        newValue.put(KEY_TO_BE_DONE, amount);
        return db.insert(DB_ACTION_TABLE, null, newValue);
    }

    public boolean updateAction(ActionTodo action) {
        long id = action.getId();
        int active = action.getActive();
        String name = action.getName();
        String type = action.getType();
        long amount = action.getAmount();
        String date = action.getDate();
        int repeat = action.getRepeat();
        String often = action.getOften();
        long toBeDone = action.getToBeDone();
        if (repeat == 0)
            return updateAction(id, active, name, type, amount, date, toBeDone);
        else
            return updateAction(id, active, name, type, amount, date, repeat, often, toBeDone);
    }

    public boolean updateAction(long id, int active, String name, String type, long amount, String date, long toBeDone) {
        String where = KEY_ID + " = " + id;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ACTIVE, active);
        newValues.put(KEY_ACTION_NAME, name);
        newValues.put(KEY_TYPE, type);
        newValues.put(KEY_AMOUNT, amount);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_TO_BE_DONE, toBeDone);
        return db.update(DB_ACTION_TABLE, newValues, where, null) > 0;
    }

    public boolean updateAction(long id, int active, String name, String type, long amount, String date, int repeat, String often, long toBeDone) {
        String where = KEY_ID +" = " + id;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ACTIVE, active);
        newValues.put(KEY_ACTION_NAME, name);
        newValues.put(KEY_TYPE, type);
        newValues.put(KEY_AMOUNT, amount);
        newValues.put(KEY_REPEAT, repeat);
        newValues.put(KEY_OFTEN, often);
        newValues.put(KEY_TO_BE_DONE, toBeDone);
        return db.update(DB_ACTION_TABLE, newValues, where, null) > 0;
    }

    public boolean deleteAction(long id) {
        String where = KEY_ID +" = " + id;
        return db.delete(DB_ACTION_TABLE, where, null) > 0;
    }

    public Cursor getAllActions() {
        String[] columns = {KEY_ID, KEY_ACTIVE, KEY_ACTION_NAME, KEY_TYPE,
                            KEY_AMOUNT, KEY_DATE, KEY_REPEAT, KEY_OFTEN};
        return db.query(DB_ACTION_TABLE, columns, null, null, null, null, null);
    }

    public ActionTodo getAction(long id) {
        String[] columns = {KEY_ID, KEY_ACTIVE, KEY_ACTION_NAME, KEY_TYPE,
                            KEY_AMOUNT, KEY_DATE, KEY_REPEAT, KEY_OFTEN, KEY_TO_BE_DONE};
        String where = KEY_ID + " = " + id;
        Cursor cursor = db.query(DB_ACTION_TABLE, columns, where, null, null, null, null);
        ActionTodo action = null;
        if(cursor != null && cursor.moveToFirst()) {
            int active = cursor.getInt(ACTIVE_COLUMN);
            String name = cursor.getString(ACTION_NAME_COLUMN);
            String type = cursor.getString(TYPE_COLUMN);
            long amount = cursor.getLong(AMOUNT_COLUMN);
            String date = cursor.getString(DATE_COLUMN);
            int repeat = cursor.getInt(REPEAT_COLUMN);
            String often = cursor.getString(OFTEN_COLUMN);
            long toBeDone = cursor.getLong(TO_BE_DONE_COLUMN);
            action = new ActionTodo(id, active, name, type, amount, date, repeat, often, toBeDone);
        }
        return action;
    }

    int actionCount() {
        String query = "SELECT "+KEY_ID+" FROM "+DB_ACTION_TABLE;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteEverything() {
        db.execSQL(DROP_ACTION_TABLE);
        db.execSQL(CREATE_ACTION_TABLE);
    }
}
