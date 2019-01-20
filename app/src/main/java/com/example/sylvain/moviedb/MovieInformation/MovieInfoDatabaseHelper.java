package com.example.sylvain.moviedb.MovieInformation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MovieInfoDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MovieInfo";
    public static final int VERSION_NUM = 1;
    public static final String TABLE_NAME = "Movies";
    public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "Title";
    public static final String KEY_YEAR = "Year";
    public static final String KEY_RATED = "Rated";
    public static final String KEY_RUNTIME = "Runtime";
    public static final String KEY_ACTORS = "Actors";
    public static final String KEY_PLOT = "Plot";

    public MovieInfoDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE " + TABLE_NAME + "( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_TITLE + " TEXT UNIQUE, " +
                KEY_YEAR + " TEXT, " +
                KEY_RATED + " TEXT, " +
                KEY_RUNTIME + " TEXT, " +
                KEY_ACTORS + " TEXT, " +
                KEY_PLOT + " TEXT" + ")");
        Log.i("ChatDatabaseHelper", "Calling OnCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.i("ChatDatabaseHelper", "Calling OnUpdate, oldVersion=" + oldVersion + " newVersion=" + newVersion);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
