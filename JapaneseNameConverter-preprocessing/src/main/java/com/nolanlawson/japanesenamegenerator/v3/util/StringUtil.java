package com.nolanlawson.japanesenamegenerator.v3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nolan
 */
public class StringUtil {

    private static final IntegerSet vowels =
            new IntegerSet(new HashSet<Character>(Arrays.asList('a','e','i','o','u')));

    public static final IntegerSet consonants = new IntegerSet(new HashSet<Character>(Arrays.asList(
            'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'z')));


    /**
     * like .toCharArray(), but strings instead of chars
     * @param str
     * @return
     */
    public static List<String> stringToListOfStrings(String str) {
        List<String> result = new ArrayList<String>();
        for (char ch : str.toCharArray()) {
            result.add(Character.toString(ch));
        }
        return result;
    }

    public static boolean isVowel(char ch) {
        return vowels.contains((int)ch);
    }

    public static boolean isConsonant(char ch) {
        return consonants.contains((int)ch);
    }

    public static boolean isConsonantOrY(char ch) {
        return consonants.contains((int)ch) || ch == 'y';
    }

    public static String concat(char char1, char... otherChars) {

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toString(char1));
        for (char ch : otherChars) {
            sb.append(Character.toString(ch));
        }
        return sb.toString();

    }
    /**
     * same as the String.split(), except it doesn't use regexes, so it's faster.
     *
     * @param str       - the string to split up
     * @param delimiter the delimiter
     * @return the split string
     */
    public static String[] quickSplit(String str, String delimiter) {
        List<String> result = new ArrayList<String>();
        int lastIndex = 0;
        int index = str.indexOf(delimiter);
        while (index != -1) {
            result.add(str.substring(lastIndex, index));
            lastIndex = index + delimiter.length();
            index = str.indexOf(delimiter, index + delimiter.length());
        }
        result.add(str.substring(lastIndex, str.length()));

        return result.toArray(new String[result.size()]);
    }

 /*
     * Replace all occurances of the searchString in the originalString with the replaceString.  Faster than the
     * String.replace() method.  Does not use regexes.
     * <p/>
     * If your searchString is empty, this will spin forever.
     *
     *
     * @param originalString
     * @param searchString
     * @param replaceString
     * @return
     */
    public static String quickReplace(String originalString, String searchString, String replaceString) {
        StringBuilder sb = new StringBuilder(originalString);
        int index = sb.indexOf(searchString);
        while (index != -1) {
            sb.replace(index, index + searchString.length(), replaceString);
            index += replaceString.length();
            index = sb.indexOf(searchString, index);
        }
        return sb.toString();
    }

    public static String join(String delimiter, String[] strings) {
        
        if (strings.length == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            stringBuilder.append(" ").append(str);
        }

        return stringBuilder.substring(1);
    }
}
