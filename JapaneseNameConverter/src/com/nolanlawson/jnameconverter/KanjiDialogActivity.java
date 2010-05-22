package com.nolanlawson.jnameconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;
import com.nolanlawson.jnameconverter.data.KanjiResultSpinnerAdapter;
import com.nolanlawson.jnameconverter.data.SharedObjects;
import com.nolanlawson.jnameconverter.data.db.KanjiEntry;
import com.nolanlawson.jnameconverter.data.db.KanjiEntryDBHelper;

public class KanjiDialogActivity extends Activity implements OnClickListener, OnItemSelectedListener {
		
	public static final String ACTION_DELETE_KANJI = "nlawson_deleteKanji";
	public static final String ACTION_EDIT_KANJI = "nlawson_editKanji";
	
	private static final String TAG = "KanjiDialogActivity";
	
	private static Pattern whitespacePattern = Pattern.compile("\\s+");
	
	private int minimumWidth;
	private String originalEnglish;
	private String roomaji;
	private HorizontalScrollView kanjiSpinnersHorizontalScrollView;
	
	private LinearLayout kanjiSpinnersLinearLayout;
	private LinearLayout kanjiEnglishContainingLinearLayout;
	private LinearLayout extraButtonsLinearLayout;
	
	private TextView kanjiTextView, roomajiTextView, englishTextView;
	private Button goBackButton, saveButton, modifyButton, shareKanjiButton, cancelButton,
					saveAlreadySavedButton, saveNewButton, deleteButton;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.kanji_dialog);
	
		setUpWidgets();
		
		if (savedInstanceState != null
				&& savedInstanceState.containsKey("savedSpinnerPositions")) {
			int[] spinnerPositions = savedInstanceState.getIntArray("savedSpinnerPositions");
			
			for (int i = 0; i < spinnerPositions.length; i++) {
				Spinner spinner = (Spinner) kanjiSpinnersLinearLayout.getChildAt(i);
				spinner.setSelection(spinnerPositions[i]);
			}
			updateKanjiAndEnglish();
		}	
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		int[] savedSpinnerPositions = new int[kanjiSpinnersLinearLayout.getChildCount()];
		
		for (int i = 0; i < kanjiSpinnersLinearLayout.getChildCount(); i++) {
			Spinner spinner = (Spinner) kanjiSpinnersLinearLayout.getChildAt(i);
			savedSpinnerPositions[i] = spinner.getSelectedItemPosition();
		}
		
		outState.putIntArray("savedSpinnerPositions", savedSpinnerPositions);
		
		super.onSaveInstanceState(outState);
	}



	private void setUpWidgets() {
		
		kanjiSpinnersLinearLayout = (LinearLayout) findViewById(R.id.kanjiSpinnersLinearLayout);
		kanjiSpinnersHorizontalScrollView = 
			(HorizontalScrollView) findViewById(R.id.kanjiSpinnersHorizontalScrollView);
		kanjiEnglishContainingLinearLayout = 
			(LinearLayout) findViewById(R.id.kanjiEnglishContainingLinearLayout);
		extraButtonsLinearLayout = 
			(LinearLayout) findViewById(R.id.additionalButtonsLinearLayout);
		
		kanjiTextView = (TextView) findViewById(R.id.kanjiTextViewInDialog);
		englishTextView = (TextView) findViewById(R.id.kanjiEnglishTextViewInDialog);
		roomajiTextView = (TextView) findViewById(R.id.kanjiRoomajiTextViewInDialog);
		
		setUpDataDisplayingWidgets();
		
		goBackButton = (Button) findViewById(R.id.goBackToKanjiViewButton);
		saveButton = (Button) findViewById(R.id.saveKanjiButton);
		modifyButton = (Button) findViewById(R.id.modifyKanjiButton);
		shareKanjiButton = (Button) findViewById(R.id.shareKanjiButton);
		cancelButton = (Button) findViewById(R.id.cancelKanjiButton);
		saveAlreadySavedButton = (Button) findViewById(R.id.saveAlreadySavedKanjiButton);
		saveNewButton = (Button) findViewById(R.id.saveNewKanjiButton);
		deleteButton = (Button) findViewById(R.id.deleteKanjiButton);
		
		for (Button button : new Button[]{
				goBackButton, saveButton, modifyButton, shareKanjiButton, cancelButton,
				saveAlreadySavedButton, saveNewButton, deleteButton}) {
			button.setOnClickListener(this);
		}
		
		Bundle extras = getIntent().getExtras();
		
		if (extras.getBoolean("alreadySavedMode")) {
			setUpAlreadySavedMode();
		}
	}

	private void setUpAlreadySavedMode() {
		// this is the case where we're viewing kanji entries in the "My Saved Kanji" list
		extraButtonsLinearLayout.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.GONE);
		saveAlreadySavedButton.setVisibility(View.VISIBLE);
		goBackButton.setVisibility(View.GONE);
		
	}

	private void setUpDataDisplayingWidgets() {
		Bundle extras = getIntent().getExtras();
		
		String kanji = extras.getString("kanji");
		kanji = makeKanjiPresentable(kanji);
		
		String[] individualKanjis = whitespacePattern.split(kanji.trim());
		List<List<KanjiResult>> rawKanjiList = SharedObjects.getRawKanjiList();
		
		for (int i = 0; i < individualKanjis.length; i++) {
			String individualKanji = individualKanjis[i];
			
			// create spinners
			Spinner spinner = new Spinner(this);
			
			List<KanjiResult> kanjiOptions = new ArrayList<KanjiResult>();
			
			for (KanjiResult kanjiResult : rawKanjiList.get(i)) {
				String currentIndividualKanji = kanjiResult.getKanji();
				if (!currentIndividualKanji.equals(individualKanji)) {
					kanjiOptions.add(kanjiResult);
				} else {
					kanjiOptions.add(0, kanjiResult);
				}
			}
			
			KanjiResultSpinnerAdapter spinnerAdapter = new KanjiResultSpinnerAdapter(this, android.R.layout.simple_spinner_item, kanjiOptions);
			spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinnerAdapter);
			
			spinner.setOnItemSelectedListener(this);
			
			kanjiSpinnersLinearLayout.addView(spinner);
			
		}
		
		kanjiTextView.setText(kanji);
		englishTextView.setText(extras.getString("english"));
		// hack to ensure that italicized text doesn't get clipped
		roomaji = extras.getString("roomaji");
		roomajiTextView.setText(Html.fromHtml("&nbsp;" + roomaji + "&nbsp;"));
		
		originalEnglish = extras.getString("originalEnglish");
		
	}

	private String makeKanjiPresentable(String kanji) {
		if (kanji.length() > 5) { // more than 3 characters (divided by 2 spaces)
			// insert a newline
			int middle = kanji.length() / 2;
			kanji = new StringBuilder(kanji.substring(0, middle).trim())
			                          .append("\n")
			                          .append(kanji.substring(middle).trim()).toString();
		}
		return kanji;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goBackToKanjiViewButton:
		case R.id.saveAlreadySavedKanjiButton:
			// modify existing kanji
			sendBackEditedKanjiResult();
			finish();
			break;
		case R.id.modifyKanjiButton:
			switchModificationMode();
			break;
		case R.id.saveNewKanjiButton:
			saveCurrentKanji();
			finish();
			break;
		case R.id.saveKanjiButton:
			saveCurrentKanji();
			notifyAboutSave();
			break;
		case R.id.shareKanjiButton:
			startActionSendActivity();
			break;
		case R.id.cancelKanjiButton:
			finish();
			break;
		case R.id.deleteKanjiButton:
			sendBackDeletedKanjiResult();
			finish();
			break;
		}
	}

	private void sendBackDeletedKanjiResult() {
		
		Intent intent = new Intent(ACTION_DELETE_KANJI);
		
		Log.d(TAG, "sending back deleted kanji intent");
		
		setResult(RESULT_OK, intent);
		
	}

	private void saveCurrentKanji() {
		
		KanjiEntryDBHelper dbHelper = new KanjiEntryDBHelper(getApplicationContext());
		
		KanjiEntry kanjiEntry = new KanjiEntry();
		
		kanjiEntry.setKanji(kanjiTextView.getText().toString().replace('\n', ' '));
		kanjiEntry.setEnglish(englishTextView.getText().toString());
		kanjiEntry.setRoomaji(roomaji);
		kanjiEntry.setOriginalEnglish(originalEnglish);
		
		dbHelper.insert(kanjiEntry);
		
		dbHelper.close();
		
	}
	
	private void notifyAboutSave() {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.kanjiSavedText), Toast.LENGTH_SHORT).show();		
	}

	private void startActionSendActivity() {
		
		// send a message to email, facebook, sms, etc.
		
		Bundle extras = new Bundle();

		String subject = getResources().getString(R.string.shareSubjectToSend);
		subject = String.format(subject, originalEnglish);
		extras.putString(Intent.EXTRA_SUBJECT, subject);
		String body = getResources().getString(R.string.shareKanjiTextToSend);
		body = String.format(body, 
				originalEnglish,
				getResources().getString(R.string.app_name),
				kanjiTextView.getText().toString().replace('\n', ' '),
				roomaji,
				englishTextView.getText().toString()
				);
		extras.putString(Intent.EXTRA_TEXT, body);
		
		Intent sendActionChooserIntent = new Intent(KanjiDialogActivity.this,SendActionChooser.class);
		
		sendActionChooserIntent.putExtras(extras);
		
		startActivity(sendActionChooserIntent);
		
	}

	private void sendBackEditedKanjiResult() {
		// save the last KanjiResult the user was editing
		
		Intent intent = new Intent(ACTION_EDIT_KANJI);
		
		Bundle extras = new Bundle();
		
		int numSyllables = kanjiSpinnersLinearLayout.getChildCount();
		
		String[] kanjiArray = new String[numSyllables];
		String[] englishArray = new String[numSyllables];
		
		for (int i = 0; i < numSyllables; i++) {
			Spinner spinner = (Spinner)(kanjiSpinnersLinearLayout.getChildAt(i));
			KanjiResult kanjiResult = (KanjiResult)(spinner.getSelectedItem());
			kanjiArray[i] = kanjiResult.getKanji();
			englishArray[i] = kanjiResult.getEnglish();
		}
		
		extras.putStringArray("editedKanji", kanjiArray);
		extras.putStringArray("editedEnglish", englishArray);
		
		intent.putExtras(extras);
		
		Log.d(TAG, "sending back extras: " + extras);
		
		setResult(RESULT_OK, intent);
	}



	private void switchModificationMode() {
		
		if (kanjiSpinnersHorizontalScrollView.getVisibility() == View.GONE) {
			Log.d(TAG, "entering modification mode");
			// enter modification mode
			kanjiSpinnersHorizontalScrollView.setVisibility(View.VISIBLE);
			kanjiTextView.setVisibility(View.GONE);
			modifyButton.setText(getResources().getString(R.string.doneModifyingText));
			
			// ensure that the window doesn't get any smaller
			minimumWidth = kanjiEnglishContainingLinearLayout.getWidth();
			Log.d(TAG, "reset minWidth to " + minimumWidth);
			kanjiEnglishContainingLinearLayout.setMinimumWidth(minimumWidth);
			
		} else {
			// exit modification mode
			Log.d(TAG, "exiting modification mode");
			kanjiSpinnersHorizontalScrollView.setVisibility(View.GONE);
			kanjiTextView.setVisibility(View.VISIBLE);
			modifyButton.setText(getResources().getString(R.string.modifyKanjiText));
			
			// reset the minWidth
			kanjiEnglishContainingLinearLayout.setMinimumWidth(1);
			minimumWidth = 1;
			Log.d(TAG, "reset minWidth to " + minimumWidth);
			
			// hack to get the layout to re-draw itself at the new minwidth
			englishTextView.setText(englishTextView.getText().toString());
		}
		
		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		updateKanjiAndEnglish();
		
	}
	
	
	
	private void updateKanjiAndEnglish() {
		// ensure that the window only resizes up - not down
		int currentWidth = kanjiEnglishContainingLinearLayout.getWidth();
		if (currentWidth > minimumWidth) {
			minimumWidth = currentWidth;
			kanjiEnglishContainingLinearLayout.setMinimumWidth(minimumWidth);
			Log.d(TAG, "reset minWidth to " + minimumWidth);
		}
		
		// user has selected a new kanji - need to replace the original kanji and english text
		
		StringBuilder kanjiStringBuilder = new StringBuilder();
		StringBuilder englishStringBuilder = new StringBuilder();
		
		for (int i = 0; i < kanjiSpinnersLinearLayout.getChildCount(); i++) {
			Spinner spinner = (Spinner) kanjiSpinnersLinearLayout.getChildAt(i);
			KanjiResult kanjiResult = (KanjiResult) (spinner.getSelectedItem());
			
			kanjiStringBuilder.append(" ").append(kanjiResult.getKanji());
			englishStringBuilder.append(" ").append(kanjiResult.getEnglish());
		}
		
		String kanji = kanjiStringBuilder.substring(1);
		kanji = makeKanjiPresentable(kanji);
		
		kanjiTextView.setText(kanji);
		
		englishTextView.setText(englishStringBuilder.substring(1));		
		
	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// do nothing
	}
	
}
