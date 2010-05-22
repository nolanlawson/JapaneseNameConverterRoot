package com.nolanlawson.jnameconverter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nolanlawson.japanesenamegenerator.v3.katakana.Katakana;

public class WritingGuideActivity extends Activity implements OnClickListener {

	
	private static final String TAG = "WritingGuideActivity";
	
	private String katakanaName;
	private char currentKatakana;
	
	private ImageView writingGuideImageView;
	private Button doneButton;
	
	private TableRow tableRow1, tableRow2;
	private TextView explanationTextView, disclaimerTextView;
	
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.writing_guide);

		getExtrasAndWidgets();
		
		createTable();
		
		if (savedInstanceState != null 
				&& savedInstanceState.containsKey("savedKatakana")) {
			currentKatakana = savedInstanceState.getChar("savedKatakana");
			setImage(currentKatakana);
			
		}
		
	}
	
	


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		if (currentKatakana != 0) {
			outState.putChar("savedKatakana", currentKatakana);
		}
		
		super.onSaveInstanceState(outState);
	}




	private void getExtrasAndWidgets() {
		
		Bundle extras = getIntent().getExtras();
		katakanaName = extras.getString("katakanaName");
		
		doneButton = (Button) findViewById(R.id.writingGuideDoneButton);
		
		doneButton.setOnClickListener(this);
		
		tableRow1 = (TableRow) findViewById(R.id.writingGuideTableRow1);
		tableRow2 = (TableRow) findViewById(R.id.writingGuideTableRow2);
		
		writingGuideImageView = (ImageView) findViewById(R.id.writingGuideImageView);
		
		explanationTextView = (TextView) findViewById(R.id.writingGuideExplanationTextView);
		disclaimerTextView = (TextView) findViewById(R.id.writingGuideTenTenDisclaimerTextView);
		
	}


	private void createTable() {
		
		for (char ch : katakanaName.toCharArray()) {
			
			if (Character.isWhitespace(ch)) {
				continue;
			}
			
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			TextView roomajiTextView = (TextView)(vi.inflate(R.layout.katakana_item, null));
			String roomaji = getRoomajiRepresentation(ch);
			roomajiTextView.setText(Html.fromHtml("<i>&nbsp;"+roomaji+"&nbsp;</i>"));
			
			Button katakanaButton = (Button)(vi.inflate(R.layout.katakana_button_item, null));
			katakanaButton.setText(Character.toString(ch));
			katakanaButton.setTextColor(roomajiTextView.getCurrentTextColor());
			// XXX HACK: Setting this to null and the resource to 0 prevents
			// this weird android bug where the background becomes a huge image
			katakanaButton.setBackgroundDrawable(null);
			katakanaButton.setBackgroundResource(0);
			katakanaButton.setOnClickListener(this);
			
			tableRow1.addView(katakanaButton);
			tableRow2.addView(roomajiTextView);
		}
	}

	private void setImage(char ch) {
		
		currentKatakana = ch;
		
		String roomaji = Katakana.reverseLookup(ch);
		boolean showDisclaimer = false;
		
		if (roomaji.startsWith("prolonged")) {
			
			roomaji = "prolonged_sound";
			showDisclaimer = true;
			disclaimerTextView.setText(getResources().getString(R.string.writingGuideDashExplanation));
		} else if (roomaji.startsWith("small_")) {
			// small kana are drawn the same as big kana.  duh
			roomaji = roomaji.substring(6);
			
			showDisclaimer = true;
			if (roomaji.equals("tsu")) {
				// small tsu
				disclaimerTextView.setText(getResources().getString(R.string.writingGuideSmallTsuExplanation));
			} else {
				// small vowel or small palatalized vowel (ya, yo, yu)
				disclaimerTextView.setText(getResources().getString(R.string.writingGuideSmallVowelExplanation));
			}
			
		} else if (roomaji.startsWith("p")) {
			disclaimerTextView.setText(getResources().getString(R.string.writingGuideMaruDisclaimerText));
		} else {
			disclaimerTextView.setText(getResources().getString(R.string.writingGuideTenTenDisclaimerText));
		}
		
		// I don't have png images for the consonants with tenten marks in them
		
		if (roomaji.startsWith("dz")) {
			roomaji = "ts" + roomaji.substring(2);
			showDisclaimer = true;
		} else if (!roomaji.equals("prolonged_sound")){
			String replacementStr = removeTenTenFromConsonant(roomaji.charAt(0));
			if (replacementStr != null) {
				roomaji = replacementStr + roomaji.substring(1);
				showDisclaimer = true;
			}
		}
		
		disclaimerTextView.setVisibility(showDisclaimer ? View.VISIBLE : View.GONE);
		
		if (roomaji.equals("hu")) {
			roomaji = "fu";
		}
		
		int resourceId = getResources().getIdentifier(
				"katakana_" + roomaji, "drawable", getPackageName());

		if (resourceId != 0) { // valid id
			writingGuideImageView.setImageResource(resourceId);
			writingGuideImageView.setVisibility(View.VISIBLE);
			explanationTextView.setVisibility(View.GONE);
			Log.d(TAG, "changed image to: " + roomaji);
		} else {
			Log.d(TAG, "couldn't get image: " + roomaji);
		}
		
		
	}

	private String getRoomajiRepresentation(char ch) {
	
		
		String result = Katakana.reverseLookup(ch);
		
		Log.d(TAG, "reverse lookup of " + ch + " is: " + result);
		
		if (ch == Katakana.PROLONGED_SOUND_MARK.getChar()) {
			result = "";
		} else if (result.startsWith("small_")) {
			if (result.endsWith("tsu") || result.endsWith("tu")) {
				// small tsu
				result = "";
			} else { // small 'i', 'a', 'ya', 'yo', etc.
				result = result.substring(6);
			}
		}
		
		return result;
		
	}

	@Override
	public void onClick(View v) {
		
		if (v.getId() == R.id.writingGuideDoneButton) {
			finish();
		} else { // it's one of the ad-hoc katakana buttons I created
			char katakana = ((Button)v).getText().toString().charAt(0);
			setImage(katakana);
			
		}
	}
	
	private String removeTenTenFromConsonant(char ch) {
		switch (ch) {
		case 'p':
		case 'b':
			return "h";
		case 'g':
			return "k";
		case 'j':
			return "sh";
		case 'd':
			return "t";
		case 'z':
			return "s";
		}
		return null;
	}
}
