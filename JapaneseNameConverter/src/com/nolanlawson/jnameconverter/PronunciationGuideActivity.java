package com.nolanlawson.jnameconverter;

import java.util.List;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nolanlawson.japanesenamegenerator.v3.pronunciation.PronunciationGuide;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;

public class PronunciationGuideActivity extends Activity implements OnTouchListener {

	private LinearLayout linearLayout;
	private String roomajiName;
	
	private TextView roomajiNameTextView;
	private TableLayout pronunciationGuideTable;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.pronunciation_guide);

		getExtrasAndWidgets();

		setTextLabels();

		createTable();

	}

	private void getExtrasAndWidgets() {
		
		Bundle extras = getIntent().getExtras();
		roomajiName = extras.getString("roomajiName");

		roomajiNameTextView = (TextView) findViewById(R.id.romaajiNameInPronunciationGuide);

		pronunciationGuideTable = (TableLayout) findViewById(R.id.pronunciationGuideTable);
		
		linearLayout = (LinearLayout) findViewById(R.id.pronunciationGuideLinearLayout);
		
		roomajiNameTextView.setOnTouchListener(this);
		linearLayout.setOnTouchListener(this);
		pronunciationGuideTable.setOnTouchListener(this);

	}

	private void setTextLabels() {
		roomajiNameTextView.setText(roomajiName.toUpperCase());

	}

	private void createTable() {

		List<Pair<String, String>> guidePairs = PronunciationGuide
				.getPronunciationGuide(roomajiName.toLowerCase());

		if (guidePairs.isEmpty()) {
			Toast t = Toast.makeText(getApplicationContext(),
					"No pronunciation guide available.", Toast.LENGTH_LONG);
			t.show();
			finish();
		}

		pronunciationGuideTable.removeAllViews(); // remove the placeholders I
													// put in the xml file

		for (Pair<String, String> guidePair : guidePairs) {

			TableRow tableRow = new TableRow(getApplicationContext());

			TextView textView1 = new TextView(getApplicationContext());
			textView1.setText(guidePair.getFirst().toUpperCase());
			textView1.setPadding(4, 4, 2, 2);
			textView1.setTypeface(Typeface.DEFAULT_BOLD);

			TextView textView2 = new TextView(getApplicationContext());

			textView2.setText(guidePair.getSecond());
			textView2.setPadding(4, 4, 2, 2);

			tableRow.addView(textView1);
			tableRow.addView(textView2);

			pronunciationGuideTable.addView(tableRow);
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
			finish();
			return true;
		}
		return false;
	}
}
