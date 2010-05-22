package com.nolanlawson.jnameconverter.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;
import com.nolanlawson.jnameconverter.R;

public class KanjiResultListAdapter extends ArrayAdapter<List<KanjiResult>> {
	
	public static final int MODE_HAS_MORE = 0;
	public static final int MODE_HAS_NO_MORE = 1;
		
	private ArrayList<List<KanjiResult>> items;
	private int mode;
	
	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param items
	 * @param mode how the kanji results are being displayed
	 */
	public KanjiResultListAdapter(Context context, int textViewResourceId,
			ArrayList<List<KanjiResult>> items, int mode) {
		
		super(context, textViewResourceId, items);
		this.mode = mode;
		this.items = items;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		List<KanjiResult> kanjiResultList = items.get(position);
		
		view = buildUpView(kanjiResultList, view, parent);
		
		return view;
	}

	private View buildUpView(List<KanjiResult> kanjiResultList, View view, ViewGroup parent) {
		
		if (kanjiResultList == null) {
			return view;
		}
		
		if (kanjiResultList instanceof DummyKanjiResult) {
			return buildUpDummyView((DummyKanjiResult)kanjiResultList, view, parent);
		} else {
			return buildUpKanjiView(kanjiResultList, view, parent);
		}		
	}

	private View buildUpKanjiView(List<KanjiResult> kanjiResultList, View view, ViewGroup parent) {
		
		Context context = parent.getContext();
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.kanji_item, null);
		}
		
		TextView kanjiView = (TextView) view.findViewById(R.id.kanjiTextView);
		TextView englishView = (TextView) view
				.findViewById(R.id.kanjiEnglishTextView);
		TextView roomajiView = (TextView) view
				.findViewById(R.id.kanjiRoomajiTextView);
		
		StringBuilder kanjiStringBuilder = new StringBuilder();
		StringBuilder englishStringBuilder = new StringBuilder();
		StringBuilder roomajiStringBuilder = new StringBuilder();

		for (KanjiResult kanjiResult : kanjiResultList) {
			kanjiStringBuilder.append(" ").append(kanjiResult.getKanji());
			englishStringBuilder.append(" ").append(
					kanjiResult.getEnglish());
			roomajiStringBuilder.append(" ").append(
					kanjiResult.getRoomaji());
		}

		// substring to cut off initial ' '
		kanjiView.setText(kanjiStringBuilder.substring(1));
		englishView.setText(englishStringBuilder.substring(1));
		roomajiView.setText(roomajiStringBuilder.substring(1));		
		
		return view;
	}

	private View buildUpDummyView(DummyKanjiResult dummyKanjiResult, View view, ViewGroup parent) {
		Context context = parent.getContext();
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.dummy_list_item, null);
			
			TextView dummyTextView = (TextView) view.findViewById(R.id.dummyListItemText);
			dummyTextView.setText(dummyKanjiResult.getDisplayText());
		}		
		
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		if (mode == MODE_HAS_MORE && position == super.getCount() - 1) { // last element
			return 1; // second type
		}
		return 0; // first type
		
	}

	@Override
	public int getViewTypeCount() {
		return mode == MODE_HAS_MORE ? 2 : 1; // two different types of results, if in generated mode
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	
}
