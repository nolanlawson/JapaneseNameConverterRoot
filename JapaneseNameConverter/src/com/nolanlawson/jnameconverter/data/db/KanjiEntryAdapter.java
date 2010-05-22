package com.nolanlawson.jnameconverter.data.db;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.jnameconverter.R;

public class KanjiEntryAdapter extends ArrayAdapter<KanjiEntry> {
	
	List<KanjiEntry> items;
	
	public KanjiEntryAdapter(Context context, int textViewResourceId,
			List<KanjiEntry> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
	
		KanjiEntry kanjiEntry = items.get(position);
		
		view = buildUpView(kanjiEntry, view, parent);
		
		return view;
	}

	private View buildUpView(KanjiEntry kanjiEntry, View view, ViewGroup parent) {
		
		if (kanjiEntry == null) {
			return view;
		}
	
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
		TextView originalEnglishTextView = (TextView) view
				.findViewById(R.id.kanjiOriginalEnglishTextView);
		
		// substring to cut off initial ' '
		kanjiView.setText(kanjiEntry.getKanji());
		englishView.setText(kanjiEntry.getEnglish());
		roomajiView.setText(kanjiEntry.getRoomaji());
		originalEnglishTextView.setText(kanjiEntry.getOriginalEnglish());
		originalEnglishTextView.setVisibility(View.VISIBLE);
		
		return view;
	}
	
}
