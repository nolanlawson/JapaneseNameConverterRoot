package com.nolanlawson.jnameconverter;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;
import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import com.nolanlawson.jnameconverter.data.SharedObjects;
import com.nolanlawson.jnameconverter.data.db.KanjiEntry;
import com.nolanlawson.jnameconverter.data.db.KanjiEntryAdapter;
import com.nolanlawson.jnameconverter.data.db.KanjiEntryDBHelper;

public class ViewSavedKanjiActivity extends ListActivity {

	private static final String TAG = "ViewSavedKanjiActivity";
	
	private KanjiEntryDBHelper dbHelper;
	private KanjiEntryAdapter adapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.kanji_list);
		
		if (dbHelper == null) {
			dbHelper = new KanjiEntryDBHelper(getApplicationContext());
		}
		
		List<KanjiEntry> kanjiEntries = dbHelper.findAll();
		
		adapter = new KanjiEntryAdapter(
				getApplicationContext(), R.layout.kanji_item, kanjiEntries);
		
		setListAdapter(adapter);
	}

	@Override
	protected void onPause() {
		
		Log.d(TAG, "onPause() called");
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		Log.d(TAG, "onResume() called");
		if (dbHelper == null) {
			dbHelper = new KanjiEntryDBHelper(getApplicationContext());
		}
		
		refreshAnyAddedKanjiEntries();
		super.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		KanjiEntry kanjiEntry = adapter.getItem(position);
		
		// have to re-generate the raw kanji results, just in case the user wants to edit
		// this result is a list of possible kanji at each syllable
		KanjiGenerator kanjiGenerator = SharedObjects.getKanjiGenerator();
		List<List<KanjiResult>> rawKanjiList = kanjiGenerator.generateKanji(kanjiEntry.getRoomaji().trim());
		
		SharedObjects.setRawKanjiList(rawKanjiList);
		
		// start up the kanji dialog activity
		Intent intent = new Intent(ViewSavedKanjiActivity.this, KanjiDialogActivity.class);
		
		Bundle extras = new Bundle();
		
		extras.putString("kanji", kanjiEntry.getKanji());
		extras.putString("english", kanjiEntry.getEnglish());
		extras.putString("roomaji", kanjiEntry.getRoomaji());
		extras.putString("originalEnglish", kanjiEntry.getOriginalEnglish());
		extras.putBoolean("alreadySavedMode", true);
		
		intent.putExtras(extras);
		
		startActivityForResult(intent, position);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// get the data back from the dialog, where the kanji may have been edited
		
		Log.d(TAG, "onActivityResult() called with data: " + data);
		
		if (dbHelper == null) {
			dbHelper = new KanjiEntryDBHelper(getApplicationContext());
		}
		
		if (data != null) {
			
			if (data.getAction().equals(KanjiDialogActivity.ACTION_DELETE_KANJI)) {
				
				// kanji deleted
				int lastClickedPosition = requestCode;
				
				KanjiEntry kanjiEntry = adapter.getItem(lastClickedPosition);
				
				adapter.remove(kanjiEntry);
				
				dbHelper.delete(kanjiEntry);
				
			} else if (data.getAction().equals(KanjiDialogActivity.ACTION_EDIT_KANJI)){ 
				
				// kanji edited
			
				Bundle extras = data.getExtras();

				// update the kanji result in the database
				
				int lastClickedPosition = requestCode;
				
				KanjiEntry kanjiEntry = adapter.getItem(lastClickedPosition);
				
				String[] editedKanji = extras.getStringArray("editedKanji");
				String[] editedEnglish = extras.getStringArray("editedEnglish");
				
				String editedKanjiAsString = StringUtil.join(" ", editedKanji);
				String editedEnglishAsString = StringUtil.join(" ", editedEnglish);
				
				adapter.remove(kanjiEntry);
				
				kanjiEntry.setKanji(editedKanjiAsString);
				kanjiEntry.setEnglish(editedEnglishAsString);
				
				adapter.insert(kanjiEntry, lastClickedPosition);
				
				dbHelper.update(kanjiEntry);
				
				Log.d(TAG, "changed KanjiEntry to: " + kanjiEntry);
			}
		}
		

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshAnyAddedKanjiEntries() {
		
		List<KanjiEntry> allEntries = dbHelper.findAll();
		// if kanji entries were added, then refresh the view
		for (int i = adapter.getCount(); i < allEntries.size(); i++) {
			adapter.add(allEntries.get(i));
			setSelection(i);
		}
		
		
	}	
	
	
	
}
