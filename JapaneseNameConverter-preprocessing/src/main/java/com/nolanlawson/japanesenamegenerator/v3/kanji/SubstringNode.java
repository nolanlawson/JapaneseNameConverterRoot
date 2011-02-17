package com.nolanlawson.japanesenamegenerator.v3.kanji;

/**
 * encapsulates an object plus a backpointer to another object
 * @author nolan
 */
public class SubstringNode {

    private String str;
    private SubstringNode previous;
    private int startIndex;
    private int endIndex;

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public SubstringNode getPrevious() {
        return previous;
    }

    public void setPrevious(SubstringNode previous) {
        this.previous = previous;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }


    public String toString() {
        return String.format("SubstringNode<%s,%d,%d>",str,startIndex,endIndex);
    }
}
