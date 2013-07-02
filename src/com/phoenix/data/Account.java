package com.phoenix.data;

import android.content.ContentValues;
import android.net.Uri;

public class Account {
	public final static String TABLE_NAME = "account";

	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_POWER= "power";
	public final static String COLUMN_NAME= "name";
	public final static String COLUMN_NUMBER = "number";
	public final static String COLUMN_PASSWORD = "password";
	public final static String COLUMN_ISON =  "ison";
	
	public final static Uri CONTENT_URI = Uri.parse("content://"
			+ PoliceProvider.URI_AUTHORITY + "/" + TABLE_NAME);

	public long mId;
	public int mPower;
	public int mIson;
	public String mName;
	public String mNumner;
	public String mPassword;
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		if(mId != 0) {
			values.put(COLUMN_ID, mId);
		}
		values.put(COLUMN_POWER, mPower);
		values.put(COLUMN_NAME, mName);
		values.put(COLUMN_NUMBER, mNumner);
		values.put(COLUMN_PASSWORD, mPassword);
		values.put(COLUMN_ISON, mIson);
		return values;
	}
}
