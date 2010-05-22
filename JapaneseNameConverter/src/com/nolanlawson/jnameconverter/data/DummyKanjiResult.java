package com.nolanlawson.jnameconverter.data;

import java.util.ArrayList;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;

/**
 * dummy stand-in for the "more combinations" option in the list
 * @author nolan
 *
 */
public class DummyKanjiResult extends ArrayList<KanjiResult> {
	
	private String displayText;
	
	public DummyKanjiResult(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	
}
