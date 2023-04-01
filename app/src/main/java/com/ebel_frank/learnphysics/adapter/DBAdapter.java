package com.ebel_frank.learnphysics.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBAdapter {

    private static DBAdapter sEventDBAdapter;
    private final EventDBHelper sEventDBHelper;
    private SQLiteDatabase myDataBase;

    private DBAdapter(Context context) {
        sEventDBHelper = new EventDBHelper(context);
        try{
            sEventDBHelper.createDataBase();
        }catch(Exception e){
            throw new Error("Unable to create database");
        }
    }

    // this method makes this class a singleton class such that only
    // one instance of the class will exists all through the application.
    public static DBAdapter getHelper(Context context) {
        if (sEventDBAdapter == null) {
            sEventDBAdapter = new DBAdapter(context.getApplicationContext());
        }
        return sEventDBAdapter;
    }

    public void addBookmarkToDB(int _id, String topicName, int topicImage) {
        SQLiteDatabase db = sEventDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventDBHelper.TOPIC_ID, _id);
        values.put(EventDBHelper.TOPIC_NAME, topicName);
        values.put(EventDBHelper.TOPIC_IMAGE, topicImage);
        db.insert("Bookmarks", null, values);
        db.close();
    }

    public void deleteBookmarkFromDB(int _id) {
        SQLiteDatabase db = sEventDBHelper.getWritableDatabase();
        String[] whereArgs = {_id+""};
        db.delete("Bookmarks", EventDBHelper.TOPIC_ID+"=?", whereArgs);
        db.close();
    }

    public Cursor query(String table){
        myDataBase = sEventDBHelper.getWritableDatabase();
        return myDataBase.query(table, null, null, null, null, null, null);
    }

    public void closeDataBase() {
        sEventDBHelper.close();
        assert myDataBase != null;
        myDataBase.close();
    }

    public static class EventDBHelper extends SQLiteOpenHelper {
        private static String DB_PATH;
        private static final String DATABASE_NAME = "PhysicsDB";    // Database name
        private static final int DATABASE_VERSION = 1;              // database version
        private static final String TOPIC_ID = "_id";
        private static final String TOPIC_NAME = "topicName";
        public static final String TOPIC_IMAGE = "topicImage";

        private final Context context;

        private EventDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            DB_PATH = "/data/data/"+context.getPackageName()+"/databases/";
        }

        // Just for the sake of semantics
        @Override
        public void onCreate(final SQLiteDatabase db) {
           // does nothing
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            if (newVersion>oldVersion)
                try{
                    copyDataBase();
                }catch (IOException e){
                    e.printStackTrace();
                }
        }
        private void createDataBase() {
            boolean dbExists = checkDataBase();
            if(!dbExists){
                this.getReadableDatabase();
                try{
                    copyDataBase();
                }catch(Exception e){
                    throw new Error("Error copying database");
                }
            }
        }
        private boolean checkDataBase() {
            SQLiteDatabase checkDB = null;
            try {
                String myPath = DB_PATH + DATABASE_NAME;
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            }catch (SQLException ignored){
            }
            if(checkDB!=null){
                checkDB.close();
            }
            return checkDB != null;
        }

        private void copyDataBase() throws IOException{
            InputStream myInput = context.getAssets().open(DATABASE_NAME);
            String outFileName = DB_PATH + DATABASE_NAME;
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[25];
            int length;
            while((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }
}