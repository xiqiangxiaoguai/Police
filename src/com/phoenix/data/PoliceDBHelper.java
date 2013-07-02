package com.phoenix.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PoliceDBHelper extends SQLiteOpenHelper{

	public PoliceDBHelper(Context context, String name,
			int version) {
		this(context, name, null, version);
	}
	
	public PoliceDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Account.TABLE_NAME + "("
				+ Account.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Account.COLUMN_POWER + " INTEGER,"
				+ Account.COLUMN_NAME + " TEXT,"
				+ Account.COLUMN_NUMBER + " TEXT,"
				+ Account.COLUMN_PASSWORD + " TEXT,"
				+ Account.COLUMN_ISON + " INTEGER"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}



}
