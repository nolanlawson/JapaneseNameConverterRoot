package com.nolanlawson.jnameconverter.data.db;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;

public class KanjiEntry {

	private int id;
	private String kanji;
	private String english;
	private String roomaji;
	private String originalEnglish;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getKanji() {
		return kanji;
	}
	public void setKanji(String kanji) {
		this.kanji = kanji;
	}
	public String getEnglish() {
		return english;
	}
	public void setEnglish(String english) {
		this.english = english;
	}
	public String getRoomaji() {
		return roomaji;
	}
	public void setRoomaji(String roomaji) {
		this.roomaji = roomaji;
	}
	public String getOriginalEnglish() {
		return originalEnglish;
	}
	public void setOriginalEnglish(String originalEnglish) {
		this.originalEnglish = originalEnglish;
	}
	
	public String toString() {
		return String.format("KanjiEntry<%d,%s,%s,%s,%s>", id, kanji, english, roomaji, originalEnglish);
	}
	
	public static KanjiEntry fromKanjiResult(KanjiResult kanjiResult, String originalEnglish) {
		
		KanjiEntry result = new KanjiEntry();
		
		result.setEnglish(kanjiResult.getEnglish());
		result.setKanji(kanjiResult.getKanji());
		result.setRoomaji(kanjiResult.getRoomaji());
		result.setOriginalEnglish(originalEnglish);
		
		return result;
	}
}
