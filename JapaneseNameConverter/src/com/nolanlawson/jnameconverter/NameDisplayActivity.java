package com.nolanlawson.jnameconverter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nolanlawson.japanesenamegenerator.v3.JapaneseNameGenerator;
import com.nolanlawson.japanesenamegenerator.v3.katakana.KatakanaConverter;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import com.nolanlawson.jnameconverter.data.SharedObjects;

public class NameDisplayActivity extends Activity implements OnClickListener, OnKeyListener {
   
	private static final String TAG = NameDisplayActivity.class.getCanonicalName();
	
	private String englishName;
	private String roomajiName;
	private String katakanaName;
	
	private Button pronunciationGuideButton, startOverButton, shareButton, 
					convertToKanjiButton, writingGuideButton;
	private TextView englishNameExplanationTextView;
	private TextView katakanaNameTextView;
	private TextView romaajiNameTextView;
	private EditText overrideRoomajiEditText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.name_display);
        
        setUpWidgets();

        fetchEnglishName();
        
        boolean success;
        
        if (savedInstanceState != null 
        		&& savedInstanceState.containsKey("savedRoomaji")
        		&& savedInstanceState.containsKey("savedKatakana")) {
        	// use the saved katakana and roomaji
        	katakanaName = savedInstanceState.getString("savedKatakana");
        	roomajiName = savedInstanceState.getString("savedRoomaji");
        	success = true;
        } else {
        	// re-convert
        	success = convertName();
        }
        
        if (success) {
        	displayNames();
        } else {
        	finish();
        }
    }
    
    

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		outState.putString("savedRoomaji", roomajiName);
		outState.putString("savedKatakana", katakanaName);
		
		super.onSaveInstanceState(outState);
	}



	private void fetchEnglishName() {
		Bundle extras = getIntent().getExtras();
		englishName = extras.getString("englishName");
		
	}

	private void displayNames() {
		
		englishNameExplanationTextView.setText(
				String.format("'%s' transliterates to: ", englishName.trim()));
		
		showRoomajiAndKatakana();
		
	}

	private boolean convertName() {

		Log.d(TAG, "Converting name to katakana: " + englishName);
		
		
		JapaneseNameGenerator japaneseNameGenerator = 
			SharedObjects.getJapaneseNameGenerator();
		
		try {
			Pair<String, String> convertedPair = 
				japaneseNameGenerator.convertToRomaajiAndKatakana(englishName);
			roomajiName = convertedPair.getFirst().toUpperCase();
			katakanaName = convertedPair.getSecond();
		} catch (Throwable e) {
			Toast.makeText(this, "Failed to convert that name...", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			Log.e(TAG, "Failed to convert that name: " + englishName, e);
			return false;
		}
		return true;
				
	}

	private void setUpWidgets() {

        pronunciationGuideButton = (Button) findViewById(R.id.pronunciationGuideButton);
        startOverButton = (Button) findViewById(R.id.startOverButton);
        shareButton = (Button) findViewById(R.id.shareKatakanaButton);
        convertToKanjiButton = (Button) findViewById(R.id.convertToKanjiButton);
        englishNameExplanationTextView = (TextView) findViewById(R.id.englishNameExplanation);
        katakanaNameTextView = (TextView) findViewById(R.id.katakanaName);
        romaajiNameTextView = (TextView) findViewById(R.id.romaajiName);
        overrideRoomajiEditText = (EditText) findViewById(R.id.overrideRoomajiEditText);
        writingGuideButton = (Button) findViewById(R.id.writingGuideButton);
        
        for (Button button : new Button[]{pronunciationGuideButton, 
        									startOverButton, 
        									shareButton,
        									convertToKanjiButton,
        									writingGuideButton}) {
        	
        	button.setOnClickListener(this);
        }
        
        overrideRoomajiEditText.setOnKeyListener(this);
		
	}

	@Override
	public void onClick(View v) {
		
		switchOutOfOverrideView();
		
		switch (v.getId()) {
		case R.id.pronunciationGuideButton:
			
			Intent pronunciationIntent = new Intent(NameDisplayActivity.this, 
					PronunciationGuideActivity.class);
			Log.i(TAG,"roomaji: " + roomajiName);
			
			pronunciationIntent.putExtra("roomajiName", roomajiName);
			
			startActivity(pronunciationIntent);			
			
			break;
		case R.id.startOverButton:
			finish();
			break;
		case R.id.shareKatakanaButton:
			startActionSendActivity();
			break;
		case R.id.convertToKanjiButton:
			Intent kanjiIntent = new Intent(NameDisplayActivity.this, GenerateKanjiActivity.class);
			
			Log.i(TAG,"converting to kanji: " + roomajiName);
			
			kanjiIntent.putExtra("roomajiName", roomajiName);
			kanjiIntent.putExtra("originalEnglish" , englishName);
			startActivity(kanjiIntent);
			
			break;
		case R.id.writingGuideButton:
			Intent writingGuideIntent = new Intent(NameDisplayActivity.this, WritingGuideActivity.class);
			
			Log.i(TAG, "starting writing guide for: " + katakanaName);
			
			writingGuideIntent.putExtra("katakanaName", katakanaName);
			startActivity(writingGuideIntent);
			break;
		}
		
	}
	
	private void startActionSendActivity() {
		
		// send a message to email, facebook, sms, etc.
		
		Bundle extras = new Bundle();

		String subject = getResources().getString(R.string.shareSubjectToSend);
		subject = String.format(subject, englishName);
		extras.putString(Intent.EXTRA_SUBJECT, subject);
		
		String body = getResources().getString(R.string.shareKatakanaTextToSend); 
		body = String.format(body, 
				englishName,
				getResources().getString(R.string.app_name),
				katakanaName.replace('\n', ' '),
				roomajiName.replace('\n', ' ').toLowerCase()
				);
		extras.putString(Intent.EXTRA_TEXT, body);
		
				
		Intent sendActionChooserIntent = new Intent(NameDisplayActivity.this,SendActionChooser.class);
		
		sendActionChooserIntent.putExtras(extras);
		
		startActivity(sendActionChooserIntent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.name_display_menu, menu);
		
		return true;
		
	}	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.overrideKatakana:
			switchToOverrideView();
			return true;
			
		}
		return false;
	}
	
	

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
			String roomaji = overrideRoomajiEditText.getText().toString();
			overrideKatakana(roomaji);
		}
		
		return false;
	}

	private void overrideKatakana(String roomaji) {
		
		boolean overrodeKatakana = false;
		
		if (roomaji != null 
				&& roomaji.trim().length() > 0 
				&& StringUtil.quickSplit(roomaji.trim(), " ").length < 3) { // less than 3 tokens only
			try {
				KatakanaConverter katakanaConverter = new KatakanaConverter();
				
				StringBuilder katakana = new StringBuilder();
				
				for (String substr : StringUtil.quickSplit(roomaji.trim().toLowerCase(), " ")) {
					katakana.append(" ").append(katakanaConverter.convertToKatakana(substr));
				}
				
				// if we made it this far, then the katakana is valid
				roomajiName = roomaji.trim().toLowerCase();
				katakanaName = katakana.toString().trim();

				showRoomajiAndKatakana();

				
				overrodeKatakana = true;
				
			} catch (Throwable t) {
				Log.w(TAG, "failed to convert user's roomaji to katakana: '" + roomaji+"'", t);
				
			}
		}
		
		switchOutOfOverrideView();
		
		if (!overrodeKatakana) {
			Log.w(TAG, "failed to convert user's roomaji to katakana: '" + roomaji+"'");
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalidRoomaji), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void showRoomajiAndKatakana() {
		
		if (katakanaName.contains(" ")) { // two tokens
			katakanaNameTextView.setLines(2);
			katakanaNameTextView.setText(katakanaName.replace(' ', '\n'));
		} else {
			katakanaNameTextView.setLines(1);
			katakanaNameTextView.setText(katakanaName);
		}				
		
		romaajiNameTextView.setText(roomajiName.toUpperCase());
		
	}



	private void switchOutOfOverrideView() {
		
		this.overrideRoomajiEditText.setVisibility(View.GONE);
		this.romaajiNameTextView.setVisibility(View.VISIBLE);
		this.overrideRoomajiEditText.setText("");
		
	}

	private void switchToOverrideView() {
		overrideRoomajiEditText.setVisibility(View.VISIBLE);
		romaajiNameTextView.setVisibility(View.GONE);
	}
}
