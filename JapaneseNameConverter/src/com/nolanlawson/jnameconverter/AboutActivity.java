package com.nolanlawson.jnameconverter;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;

public class AboutActivity extends Activity implements OnTouchListener {

	private ScrollView scrollView;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		scrollView = (ScrollView) findViewById(R.id.aboutScrollView);
		scrollView.setOnTouchListener(this);
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		finish();
		return true;
	}
}
