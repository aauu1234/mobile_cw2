package org.tensorflow.lite.examples.classification

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class MyDatabaseHelper(var context:Context,name:String,version:Int):SQLiteOpenHelper(context,name,null,version) {
    public var createPlant="create table Plant ("+
            "id integer primary key autoincrement,"+
            "plantname text,"+
            "status text,"+
            "info text,"+
            "drugEffect text,"+
            "curing text,"+
            "image blob)"

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL(createPlant)
        Toast.makeText(context,"Create Success",Toast.LENGTH_LONG).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

        db?.execSQL("drop table if exists Plant")
        onCreate(db)
    }


}