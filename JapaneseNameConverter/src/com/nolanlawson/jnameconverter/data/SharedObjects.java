package com.nolanlawson.jnameconverter.data;

import java.util.List;

import com.nolanlawson.japanesenamegenerator.v3.JapaneseNameGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;

public class SharedObjects {

	private static JapaneseNameGenerator japaneseNameGenerator;
	private static String typedText;
	private static KanjiGenerator kanjiGenerator;
	private static List<List<KanjiResult>> rawKanjiList;
	
	public static String getTypedText() {
		return typedText;
	}
	
	public static void setTypedText(String typedText) {
		SharedObjects.typedText = typedText;
	}
	
	public static JapaneseNameGenerator getJapaneseNameGenerator() {
		return japaneseNameGenerator;
	}
	
	public static void setJapaneseNameGenerator(JapaneseNameGenerator japaneseNameGenerator) {
		SharedObjects.japaneseNameGenerator = japaneseNameGenerator;
	}

	public static KanjiGenerator getKanjiGenerator() {
		return kanjiGenerator;
	}

	public static void setKanjiGenerator(KanjiGenerator kanjiGenerator) {
		SharedObjects.kanjiGenerator = kanjiGenerator;
	}

	public static List<List<KanjiResult>> getRawKanjiList() {
		return rawKanjiList;
	}

	public static void setRawKanjiList(List<List<KanjiResult>> rawKanjiList) {
		SharedObjects.rawKanjiList = rawKanjiList;
	}
	
}
