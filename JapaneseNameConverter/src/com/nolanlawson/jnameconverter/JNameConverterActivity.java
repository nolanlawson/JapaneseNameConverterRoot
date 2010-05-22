package com.nolanlawson.jnameconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nolanlawson.japanesenamegenerator.v3.JapaneseNameGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiGenerator;
import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import com.nolanlawson.jnameconverter.data.SharedObjects;

public class JNameConverterActivity extends Activity 
        implements OnKeyListener, OnClickListener, Runnable {
    
	private static final String TAG = JNameConverterActivity.class.getCanonicalName();
	private static Pattern letterPattern = Pattern.compile("[a-zA-Z]");
	
	private Button convertButton, aboutButton, viewSavedKanjiButton;
	private EditText englishNameEditText;
	
	private Handler handler = getHandler();
	private ProgressDialog progressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        setUpWidgets();
        
    }
    
    @Override 
    public void onConfigurationChanged(Configuration newConfig)  {
    	Log.d(TAG, "configuration changed: " + newConfig);
    	super.onConfigurationChanged(newConfig);
    }
    
	private void loadModelsIfNecessary() {
		if (SharedObjects.getJapaneseNameGenerator() == null ||
				SharedObjects.getKanjiGenerator() == null) {
			
			progressDialog = ProgressDialog.show(this, "Please Wait...", "Loading data", true, false);
	        
	        Thread thread = new Thread(this);
	        thread.start();
		}
		
	}

	private Handler getHandler() {
		
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                    if (progressDialog != null) {
                    	progressDialog.dismiss();
                    }
            }
        };
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume() called");
		super.onResume();
		loadModelsIfNecessary();
		this.englishNameEditText.setText(SharedObjects.getTypedText());	
		
	}
	
	

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause() called");
		super.onPause();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		
		// save the text the user has typed
		SharedObjects.setTypedText(this.englishNameEditText.getText().toString());
	}
	
	private void setUpModels() {
		
		Log.d(TAG, "setting up the models...");
		
		if (SharedObjects.getJapaneseNameGenerator() == null) {
		
			AssetManager assetManager = getAssets();
			
			JapaneseNameGenerator japaneseNameGenerator;
			
			try {
				String roomajiModelName = getResources().getString(R.string.roomajiModel);
				InputStream romaajiModelInputStream = assetManager.open(roomajiModelName);
				
				String directLookupName = getResources().getString(R.string.nameLookup);
				InputStream directLookupInputStream = assetManager.open(directLookupName);
				long startTime = System.currentTimeMillis();
				japaneseNameGenerator = 
					new JapaneseNameGenerator(romaajiModelInputStream, directLookupInputStream);
				Log.d(TAG, "Took " + (System.currentTimeMillis() - startTime) + "ms to load roomaji model");
			} catch (IOException ex) {
				Log.e(TAG, "couldn't load asset", ex);
				throw new RuntimeException("Couldn't load asset",ex);
			}
			
			SharedObjects.setJapaneseNameGenerator(japaneseNameGenerator);
		}
		
		
		if (SharedObjects.getKanjiGenerator() == null) {
			try {
				String kanjiDictionaryFilename = getResources().getString(
						R.string.kanjiDictionary);
				InputStream kanjiDictionaryFileInputStream = getAssets().open(
						kanjiDictionaryFilename);
				
				
				long startTime = System.currentTimeMillis();
				KanjiGenerator kanjiGenerator = new KanjiGenerator(
						kanjiDictionaryFileInputStream);
				Log.d(TAG, "Took " + (System.currentTimeMillis() - startTime)
						+ "ms to load kanji dict");
				SharedObjects.setKanjiGenerator(kanjiGenerator);
			} catch (IOException ex) {
				Log.e(TAG, "unable to read in kanji dictionary", ex);
				throw new RuntimeException(
						"unable to read in kanji dictionary", ex);
			}
		}
			
		// done setting up the models; send a message to the handler so it can remove the
		// progress bar
		handler.sendEmptyMessage(0);
				
	}

	private void setUpWidgets() {
		
        convertButton = (Button) findViewById(R.id.convertButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
        englishNameEditText = (EditText) findViewById(R.id.englishNameEditText);
        viewSavedKanjiButton = (Button) findViewById(R.id.viewSavedKanjiButton);
        
        
        for (Button button : new Button[]{convertButton,aboutButton, viewSavedKanjiButton}) {
        	button.setOnClickListener(this);
        }
        
        englishNameEditText.setOnKeyListener(this); 
		
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.convertButton:
			switchToNameViewActivity();
			break;
		case R.id.aboutButton:
			switchToAboutActivity();
			break;
		case R.id.viewSavedKanjiButton:
			switchToViewSavedKanjiActivity();
			break;
		}
		
	}

	private void switchToViewSavedKanjiActivity() {
		
		Intent intent = new Intent(JNameConverterActivity.this, ViewSavedKanjiActivity.class);
		
		startActivity(intent);
		
	}

	private void switchToAboutActivity() {
		Intent intent = new Intent(JNameConverterActivity.this, 
				AboutActivity.class);
		
		startActivity(intent);		
	}

	private void switchToNameViewActivity() {
		
		String englishName = englishNameEditText.getText().toString();
		
        if (englishName == null || englishName.trim().equals("")) {
            Toast t = Toast.makeText(getApplicationContext(), "Whoops, no text!", Toast.LENGTH_SHORT);
            t.show();		
            englishNameEditText.setText("");
        } else if (StringUtil.quickSplit(englishName.trim(), " ").length > 2) {
        	Toast t = Toast.makeText(getApplicationContext(), "Two names maximum, please!", Toast.LENGTH_SHORT);
        	t.show();
        } else if (!letterPattern.matcher(englishName).find()){
        	Toast t = Toast.makeText(getApplicationContext(), "Whoops, no letters in that name!", Toast.LENGTH_SHORT);
        	t.show();        	
        } else {
			Intent intent = new Intent(JNameConverterActivity.this, 
					NameDisplayActivity.class);
			Log.i(JNameConverterActivity.class.getCanonicalName(),
					"name: " + englishNameEditText.getText().toString());
			
			intent.putExtra("englishName", englishName);
			
			// erase the name they've already typed in so it will go away when they click 'back'
			SharedObjects.setTypedText("");
			englishNameEditText.setText("");
			
			startActivity(intent);
        }
		
	}
    
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_ENTER 
				&& event.getAction() == KeyEvent.ACTION_UP) {
			// pressing 'return' redirects to the 'convert' button
			this.convertButton.performClick();
			return true;
		}
		
		return false;
	}
	

	@Override
	public void run() {
		setUpModels();
		
	}
}