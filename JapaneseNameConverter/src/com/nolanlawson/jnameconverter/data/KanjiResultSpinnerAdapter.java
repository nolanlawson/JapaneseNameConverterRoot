package com.nolanlawson.jnameconverter.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;

public class KanjiResultSpinnerAdapter extends ArrayAdapter<KanjiResult> {
	
	private List<KanjiResult> items;
	private int textViewResourceId;
	private int dropDownResourceId;
	
	public KanjiResultSpinnerAdapter(Context context, int textViewResourceId,
			List<KanjiResult> items) {
		
		super(context, textViewResourceId, items);
		this.items = items;
		this.textViewResourceId = textViewResourceId;
	}
	
	
	
	@Override
	public View getDropDownView(int position, View view, ViewGroup parent) {
		return getViewGivenResource(position, view, parent, dropDownResourceId);
	}
	
	@Override
	public void setDropDownViewResource(int resource) {
		super.setDropDownViewResource(resource);
		this.dropDownResourceId = resource;
	}



	@Override
	public View getView(int position, View view, ViewGroup parent) {
		return getViewGivenResource(position, view, parent, textViewResourceId);
	}
	
	private View getViewGivenResource(int position, View view, ViewGroup parent, int resource) {
		Context context = parent.getContext();
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(resource, null);
		}		

		TextView textView = (TextView) view;
		KanjiResult item = items.get(position);
		if (resource == textViewResourceId) { //main view when not in dropdown mode
			// so keep it small - just use the kanji
			textView.setText(item.getKanji());
			textView.setTextSize(textView.getTextSize() * 2);  // double the default android size
		} else {
			textView.setText(item.getKanji() + " - " + item.getEnglish());
		}
		return textView;		
	}
}
