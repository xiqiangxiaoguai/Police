package com.phoenix.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PoliceProvider extends ContentProvider{

	public static final String DB_NAME = "police.db";
	public static final String URI_AUTHORITY = "com.phoenix.police";
	
	private static final int URI_CODE_ACCOUNT = 1;
	private static final int URI_CODE_ACCOUNT_ID = 2;
	
	public static final String URI_MIME_ACCOUNT 
		= "vnd.android.cursor.dir/vnd.phoenix.account";
	public static final String URI_ITEM_MIME_ACCOUNT
		= "vnd.android.cursor.item/vnd.phoenix.account";
	
	private static final String TAG = PoliceProvider.class.getSimpleName();
	
	private static UriMatcher mUriMatcher;
	private PoliceDBHelper mPoliceDBHelper;
	
	static{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(URI_AUTHORITY, Account.TABLE_NAME, URI_CODE_ACCOUNT);
		mUriMatcher.addURI(URI_AUTHORITY, Account.TABLE_NAME + "/#", URI_CODE_ACCOUNT_ID );
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count =0;
		SQLiteDatabase db = mPoliceDBHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_ACCOUNT:
			count = db.delete(Account.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_ACCOUNT_ID:
			count = db.delete(Account.TABLE_NAME, Account.COLUMN_ID
					+ "="
					+ uri.getLastPathSegment()
					+ (!TextUtils.isEmpty(selection) ? " AND ("
					+ selection + ")" : ""), selectionArgs);

		default:
			Log.e(TAG, "Unknown URI:" + uri);
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(mUriMatcher.match(uri)){
			case URI_CODE_ACCOUNT:
				return URI_MIME_ACCOUNT;
			case URI_CODE_ACCOUNT_ID:
				return URI_ITEM_MIME_ACCOUNT;
			default:
				Log.e(TAG, "Unknown URI:" + uri);
	            throw new IllegalArgumentException("Unknown URI:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId;
		Uri rowUri = null;
		SQLiteDatabase db = mPoliceDBHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_ACCOUNT:
			rowId = db.insert(Account.TABLE_NAME, null, values);
			if(rowId != -1){
				rowUri = ContentUris.withAppendedId(Account.CONTENT_URI, rowId);
			}
			break;
		}
		db.close();
		return rowUri;
	}

	@Override
	public boolean onCreate() {
		mPoliceDBHelper = new PoliceDBHelper(getContext(), DB_NAME, 1);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor =null;
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
			case URI_CODE_ACCOUNT:
				sqlBuilder.setTables(Account.TABLE_NAME);
				break;
			case URI_CODE_ACCOUNT_ID:
				sqlBuilder.setTables(Account.TABLE_NAME);
				sqlBuilder.appendWhere(Account.COLUMN_ID + "="
						+ uri.getLastPathSegment());
				break;
			default:
				Log.e(TAG, "Unknown URI:" + uri);
				throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		cursor = sqlBuilder.query(mPoliceDBHelper.getReadableDatabase(), projection,
				selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mPoliceDBHelper.getReadableDatabase();
		int count ;
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_ACCOUNT  :
			count = db.update(Account.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_ACCOUNT_ID:
			count = db.update(Account.TABLE_NAME, values, Account.COLUMN_ID
					+ "="
					+ uri.getLastPathSegment()
					+ (!TextUtils.isEmpty(selection) ? " AND ("
							+ selection + ")" : ""), selectionArgs);
		default:
			Log.e(TAG, "Unknown URI:" + uri);
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}

}
