package com.nolanlawson.japanesenamegenerator.v3.kanji;

/**
 *
 * @author nolan
 */
public class KanjiResult {

    private String kanji;
    private String roomaji;
    private String english;

    public KanjiResult() {
    }
    
    public KanjiResult(String kanji, String roomaji, String english) {
        this.kanji = kanji;
        this.roomaji = roomaji;
        this.english = english;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getRoomaji() {
        return roomaji;
    }

    public void setRoomaji(String roomaji) {
        this.roomaji = roomaji;
    }

    public String toString() {
        return "<"+kanji+","+roomaji+"," + english +">";
    }
    
    public KanjiResult clone() {
    	KanjiResult clone = new KanjiResult();
    	clone.setEnglish(english);
    	clone.setKanji(kanji);
    	clone.setRoomaji(roomaji);
    	return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((english == null) ? 0 : english.hashCode());
        result = prime * result + ((kanji == null) ? 0 : kanji.hashCode());
        result = prime * result + ((roomaji == null) ? 0 : roomaji.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KanjiResult other = (KanjiResult) obj;
        if (english == null) {
            if (other.english != null)
                return false;
        } else if (!english.equals(other.english))
            return false;
        if (kanji == null) {
            if (other.kanji != null)
                return false;
        } else if (!kanji.equals(other.kanji))
            return false;
        if (roomaji == null) {
            if (other.roomaji != null)
                return false;
        } else if (!roomaji.equals(other.roomaji))
            return false;
        return true;
    }
}
