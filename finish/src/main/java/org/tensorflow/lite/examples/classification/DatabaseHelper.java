package org.tensorflow.lite.examples.classification;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.Tag;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    final private static int DATABASE_VERSION = 1;
    final private static String DATABASE_NAME = "PlantStore.db";

    private AssetManager assets;
    private String databaseDir;
    private static final String TAG = "DB";
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        assets = context.getAssets();
        Log.d(TAG,"dsaLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL"+assets);
        databaseDir = context.getApplicationInfo().dataDir + "/databases/";
        Log.d(TAG,"databaseDir"+databaseDir);
        File file = new File(databaseDir);
        //file.mkdir();
        if(!file.exists()) {
            Log.d(TAG,"FUCK");
            file.mkdir();

        }else{
            Log.d(TAG,"YOU");
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        copyDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if(!isDatabaseExist())
            copyDatabase();
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if(!isDatabaseExist())
            copyDatabase();
        return super.getReadableDatabase();
    }


    private Boolean isDatabaseExist() {
        return new File(databaseDir + DATABASE_NAME).exists();
    }

    private void copyDatabase() {

        try {
            Log.d(TAG,"dsaPLACEPLACEPLACE"+assets);
            InputStream inputStream = assets.open("databases/" + DATABASE_NAME);
            Log.d(TAG,"dBBBBBBBBBBBBBBBBBBBBBBBB"+inputStream.read());
            FileOutputStream outputStream = new FileOutputStream(databaseDir + DATABASE_NAME);

            byte[] buffer = new byte[8 * 1024];

            int readed;
            while ((readed = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readed);
            }

            outputStream.flush();

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
