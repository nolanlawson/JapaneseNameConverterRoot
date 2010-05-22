package com.nolanlawson.jnameconverter.data.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KanjiEntryDBHelper extends SQLiteOpenHelper {
		
	//constants
	private static final String TAG = "KanjiEntryDBHelper";
	
	// schema constants
	private static final String DB_NAME = "kanji_entries.db";
	private static final int DB_VERSION = 1;
	
	
	// table constants
	public static final String TABLE_NAME = "kanjiEntries";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_KANJI = "kanji";
	public static final String COLUMN_ENGLISH = "english";
	public static final String COLUMN_ROOMAJI = "roomaji";
	public static final String COLUMN_ORIGINAL_ENGLISH = "originalEnglish";
	
	// private variables
	private SQLiteDatabase db;
	
	public KanjiEntryDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		db = getWritableDatabase();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "create table %s " +
				"(" +
				"%s integer not null primary key autoincrement, " +
				"%s text not null, " +
				"%s text not null, " +
				"%s text not null, " +
				"%s text not null" +
				");";
		
		sql = String.format(sql,TABLE_NAME, COLUMN_ID, COLUMN_KANJI, COLUMN_ENGLISH, 
				COLUMN_ROOMAJI, COLUMN_ORIGINAL_ENGLISH);
		
		Log.i(TAG, "created the database");
		
		db.execSQL(sql);
	}
	
	public List<KanjiEntry> findAll() {
		
		Cursor cursor = db.query(TABLE_NAME, 
        		new String[]{COLUMN_ID, 
        		COLUMN_KANJI,
        		COLUMN_ENGLISH,
        		COLUMN_ROOMAJI,
        		COLUMN_ORIGINAL_ENGLISH}, 
        		null, null, null, null,	null);	
		
		List<KanjiEntry> result = new ArrayList<KanjiEntry>();
		
		while (cursor.moveToNext()) {
    		KanjiEntry kanjiEntry = new KanjiEntry();
    		
    		kanjiEntry.setId(cursor.getInt(0));
    		kanjiEntry.setKanji(cursor.getString(1));
    		kanjiEntry.setEnglish(cursor.getString(2));
    		kanjiEntry.setRoomaji(cursor.getString(3));
    		kanjiEntry.setOriginalEnglish(cursor.getString(4));
    		
    		result.add(kanjiEntry);
        }		
        
		cursor.close();
		
        Log.d(TAG, "returning " + result.size() + " kanjiEntries from the DB");
        
		return result;
	}
	
	public int update(KanjiEntry kanjiEntry) {
		
		ContentValues contentValues = getContentValues(kanjiEntry);
		
		int result = db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + kanjiEntry.getId(), null);
				
		Log.d(TAG, "updated kanjiEntry: " + kanjiEntry);
		
		return result;
	}
	
	public long insert(KanjiEntry kanjiEntry) {
		
		ContentValues contentValues = getContentValues(kanjiEntry);
		
		long rowId = db.insert(TABLE_NAME, null, contentValues);
		
		Log.d(TAG, "inserted kanjiEntry: " + kanjiEntry + " with rowId: " + rowId);
		
		return rowId;	
	}
	
	public int delete(KanjiEntry kanjiEntry) {
		
		int result = db.delete(TABLE_NAME, COLUMN_ID + "=" + kanjiEntry.getId(), null);
		
		Log.d(TAG, "deleted kanjiEntry: " + kanjiEntry);
		
		return result;
	}
	
	@Override
	public void close() {
		super.close();
		if (db != null && db.isOpen()) { // just to be safe
			db.close();
		} else {
			Log.d(TAG, "db was already closed");
		}
	}

	private ContentValues getContentValues(KanjiEntry kanjiEntry) {
		
		ContentValues result = new ContentValues();
		
		result.put(COLUMN_KANJI, kanjiEntry.getKanji());
		result.put(COLUMN_ENGLISH, kanjiEntry.getEnglish());
		result.put(COLUMN_ROOMAJI, kanjiEntry.getRoomaji());
		result.put(COLUMN_ORIGINAL_ENGLISH, kanjiEntry.getOriginalEnglish());
		
		return result;
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
