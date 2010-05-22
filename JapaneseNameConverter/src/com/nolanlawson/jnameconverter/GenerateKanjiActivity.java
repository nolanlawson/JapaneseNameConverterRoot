package com.nolanlawson.jnameconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;
import com.nolanlawson.jnameconverter.data.DummyKanjiResult;
import com.nolanlawson.jnameconverter.data.KanjiResultListAdapter;
import com.nolanlawson.jnameconverter.data.SharedObjects;

public class GenerateKanjiActivity extends ListActivity {

	
	
	private static final String TAG = GenerateKanjiActivity.class.getCanonicalName();
	private static final int DEFAULT_LIST_THRESHOLD = 9;
	private static Random random = new Random();
	
	private String originalEnglish;
	private ArrayList<List<KanjiResult>> kanjiList;
	private List<List<KanjiResult>> rawKanjiList;
	private KanjiResultListAdapter adapter;
	private int listIndexThreshold = DEFAULT_LIST_THRESHOLD;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.kanji_list);
		
		Bundle extras = getIntent().getExtras();
		String roomajiName = extras.getString("roomajiName");
		originalEnglish = extras.getString("originalEnglish");
		
		Log.d(TAG, "Getting kanji for roomaji name: " + roomajiName);
		
		generateKanjiList(roomajiName);
		
		displayListAdapter();
			
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// get the data back from the dialog, where the kanji may have been edited
		
		Log.d(TAG, "onActivityResult() called with data: " + data);

		
		if (data != null && data.getAction().equals(KanjiDialogActivity.ACTION_EDIT_KANJI)) {

			// kanji was edited!
			Bundle extras = data.getExtras();
			// update the kanji result in the list
			String[] editedKanji = extras.getStringArray("editedKanji");
			String[] editedEnglish = extras.getStringArray("editedEnglish");
			int lastClickedPosition = requestCode;
			List<KanjiResult> kanjiResults = adapter.getItem(lastClickedPosition);
			
			adapter.remove(kanjiResults);
			
			kanjiResults = new ArrayList<KanjiResult>(kanjiResults);
			
			for (int i = 0; i < kanjiResults.size(); i++) {
				KanjiResult kanjiResult = kanjiResults.get(i);
				kanjiResult.setEnglish(editedEnglish[i]);
				kanjiResult.setKanji(editedKanji[i]);
			}
			
			adapter.insert(kanjiResults, lastClickedPosition);
			
			Log.d(TAG, "changed KanjiResults to: " + kanjiResults);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}



	private void displayListAdapter() {
		
		ArrayList<List<KanjiResult>> truncatedKanjiList = new ArrayList<List<KanjiResult>>(kanjiList);
		
		boolean needToTruncate = truncatedKanjiList.size() > listIndexThreshold;
		
		if (needToTruncate) {
			truncatedKanjiList = new ArrayList<List<KanjiResult>>(kanjiList.subList(0, listIndexThreshold));
			truncatedKanjiList.add(new DummyKanjiResult(getResources().getString(R.string.moreCombinations)));
		}
		
		int mode = needToTruncate ? KanjiResultListAdapter.MODE_HAS_MORE 
				: KanjiResultListAdapter.MODE_HAS_NO_MORE;
		
		adapter = new KanjiResultListAdapter(getApplicationContext(),
				R.layout.kanji_item, truncatedKanjiList, mode);
		
		setListAdapter(adapter);			
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		List<KanjiResult> item = adapter.getItem(position);
		
		if (item instanceof DummyKanjiResult) { // 'more' option
			
			// expand the list
			
			int limit = Math.min(listIndexThreshold + DEFAULT_LIST_THRESHOLD, kanjiList.size());
			
			for (int i = listIndexThreshold; i < limit; i++) {
				adapter.insert(kanjiList.get(i), adapter.getCount() - 1);
			}
			
			listIndexThreshold += DEFAULT_LIST_THRESHOLD;
			
			// check to see if we need to hide the "More Combinations" option
			
			if (listIndexThreshold >= kanjiList.size()) {
				adapter.remove(adapter.getItem(adapter.getCount() - 1));
				adapter.setMode(KanjiResultListAdapter.MODE_HAS_NO_MORE);
			}
			
			
		} else { // normal list option
			startKanjiDialogActivity(v, position);
		}
	}
	
	private ArrayList<List<KanjiResult>> generateKanjiList(String roomajiName) {
		
		KanjiGenerator kanjiGenerator = SharedObjects.getKanjiGenerator();

		if (kanjiGenerator == null) {
			Log.e(TAG, "this is bad!  kanjiGenerator is null!");
			throw new RuntimeException("this is bad!  kanjiGenerator is null!");
			
		}
		// this result is a list of possible kanji at each syllable
		rawKanjiList = kanjiGenerator.generateKanji(roomajiName);
		
		if (rawKanjiList.isEmpty()) {
			Log.d(TAG,"Couldn't generate kanji for: " + roomajiName);
			return new ArrayList<List<KanjiResult>>();
		}
		
		convertRawKanjiListToViewableKanjiResultList();
		
		return kanjiList;
	}
	
	private void convertRawKanjiListToViewableKanjiResultList() {
		
		// Try to make as varied a list as possible by mixing together different syllables
		// We're basically standing a 2d-array on its head - x becomes y and y becomes x
		// (And we're also padding some values)
		
		int longestNumKanjiForASingleSyllable = 0;
		
		// figure out how many kanji there are in the syllable that has the most
		// we'll generate that many results in the list
		for (List<KanjiResult> kanjiResultList : rawKanjiList) {
			if (kanjiResultList.size() > longestNumKanjiForASingleSyllable) {
				longestNumKanjiForASingleSyllable = kanjiResultList.size();
			}
		}
		
		kanjiList = new ArrayList<List<KanjiResult>>();
		
		for (int i = 0; i < longestNumKanjiForASingleSyllable; i++) {
			List<KanjiResult> visibleListEntry = new ArrayList<KanjiResult>();
			for (List<KanjiResult> kanjiResultList : rawKanjiList) {
				// just pick a random one in the list if we're already past the maximum
				int idx = i >= kanjiResultList.size() ? random.nextInt(kanjiResultList.size()) : i;
				KanjiResult clonedKanjiResult = kanjiResultList.get(idx).clone();
				visibleListEntry.add(clonedKanjiResult);
			}
			kanjiList.add(visibleListEntry);
		}
	}

	private void startKanjiDialogActivity(View v, int position) {
		
		View viewAtPosition = adapter.getView(position, v, this.getListView());
		
		TextView kanjiView = (TextView) viewAtPosition.findViewById(R.id.kanjiTextView);
		TextView englishView = (TextView) viewAtPosition
				.findViewById(R.id.kanjiEnglishTextView);
		TextView roomajiView = (TextView) viewAtPosition
				.findViewById(R.id.kanjiRoomajiTextView);		
		
		Bundle extras = new Bundle();
		
		extras.putString("kanji", kanjiView.getText().toString());
		extras.putString("english", englishView.getText().toString());
		extras.putString("roomaji", roomajiView.getText().toString());
		extras.putString("originalEnglish", originalEnglish);
		extras.putBoolean("alreadySavedMode", false);
		
		Intent intent = new Intent(GenerateKanjiActivity.this, KanjiDialogActivity.class);
		
		intent.putExtras(extras);
		
		SharedObjects.setRawKanjiList(rawKanjiList);
		
		startActivityForResult(intent, position);
		
	}
	
	
}
