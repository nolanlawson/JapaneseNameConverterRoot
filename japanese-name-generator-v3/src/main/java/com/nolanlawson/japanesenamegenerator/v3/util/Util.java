package com.nolanlawson.japanesenamegenerator.v3.util;

import java.lang.ref.SoftReference;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author nolan
 */
public class Util {

    private static Map<Set<CharSequence>,SoftReference<Integer>> editDistanceCache =
            new HashMap<Set<CharSequence>, SoftReference<Integer>>();

    private static Integer getFromCache(CharSequence str1, CharSequence str2) {
        Set<CharSequence> key = new PairSet<CharSequence>(str1, str2);

        SoftReference<Integer> softReference = editDistanceCache.get(key);
        return softReference != null ? softReference.get() : null;
    }

    private static void putInCache(CharSequence str1, CharSequence str2, int editDistance) {
        StringBuilder sb1 = new StringBuilder(str1);
        sb1.trimToSize();
        StringBuilder sb2 = new StringBuilder(str2);
        sb2.trimToSize();
        
        Set<CharSequence> key = new PairSet<CharSequence>(sb1, sb2);

        editDistanceCache.put(key, new SoftReference<Integer>(editDistance));
    }

    private static class PairSet<E> extends AbstractSet<E> {

        private E first;
        private E second;
        private int hashCode;

        public PairSet(E first, E second) {
            this.first = first;
            this.second = second;
            this.hashCode = (17 ^ first.hashCode()) + (17 ^ second.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass() != obj.getClass()) {
                return false;
            }
            PairSet<E> other = (PairSet<E>) obj;
            return (this.first.equals(other.first) && this.second.equals(other.second))
                    || (this.second.equals(other.first) && this.first.equals(other.second));
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public Iterator<E> iterator() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int size() {
            return 2;
        }

    }

    public static final Set<Character> consonants = new HashSet<Character>(Arrays.asList(
            'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'));
    public static final Set<Character> vowels = new HashSet<Character>(Arrays.asList('a', 'e', 'i', 'o', 'u'));

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
        StringBuffer sb = new StringBuffer(originalString);
        int index = sb.indexOf(searchString);
        while (index != -1) {
            sb.replace(index, index + searchString.length(), replaceString);
            index += replaceString.length();
            index = sb.indexOf(searchString, index);
        }
        return sb.toString();
    }

    public static List<String> splitIntoJapaneseSyllables(String str) {

        if (str.equals("")) {
            throw new RuntimeException("string can't be empty: " + str);
        }

        List<List<Character>> prelimResult = new ArrayList<List<Character>>();

        boolean lastCharWasVowel = false;

        char[] charArray = str.toCharArray();

        List<Character> currentSubsequence = new ArrayList<Character>();

        for (int i = 0; i < charArray.length; i++) {

            char ch = charArray[i];

            boolean isVowel = vowels.contains(ch);

            if (i > 0) {
                if (!isVowel) {
                    if ((!(!lastCharWasVowel && ch == 'y'))) {
                        if (((lastCharWasVowel || charArray[i - 1] == 'n')) || ch == "'".charAt(0) || (ch == 'n' && (lastCharWasVowel || charArray[i - 1] == 'n'))) {
                            prelimResult.add(currentSubsequence);
                            currentSubsequence = new ArrayList<Character>();
                        }
                    }
                } else {
                    if ((ch == 'a' && charArray[i - 1] == 'e') || (ch == 'a' && charArray[i - 1] == 'i') || (ch == 'a' && charArray[i - 1] == 'u')) {
                        prelimResult.add(currentSubsequence);
                        currentSubsequence = new ArrayList<Character>();
                    }
                }
            }

            if (ch != "'".charAt(0)) {
                currentSubsequence.add(ch);
            }

            lastCharWasVowel = isVowel;

        }
        prelimResult.add(currentSubsequence);

        List<String> result = new ArrayList<String>();

        for (List<Character> charList : prelimResult) {
            char[] currCharArray = new char[charList.size()];

            for (int i = 0; i < charList.size(); i++) {
                currCharArray[i] = charList.get(i);
            }
            result.add(new String(currCharArray));
        }

        return result;
    }

    private static int findCommonPrefixLength(CharSequence str1, CharSequence str2) {

        int length = (Math.min(str1.length(), str2.length()));
        for (int i = 0; i < length; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return i;
            }
        }

        return 0;

    }

    private static int findCommonSuffixLength(CharSequence str1, CharSequence str2, int commonPrefixLength) {
        int length = (Math.min(str1.length(), str2.length()));
        for (int i = 0; i < length - commonPrefixLength; i++) {
            if (str1.charAt(str1.length() - i - 1) != str2.charAt(str2.length() - i - 1)) {
                return i;
            }
        }

        return 0;
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {

        /*Integer cached = getFromCache(str1, str2);

        if (cached != null) {
            return cached;
        }*/

        int commonPrefixLength = findCommonPrefixLength(str1, str2);

        if (commonPrefixLength == str1.length() && commonPrefixLength == str2.length()) {
            return 0; // same exact string
        }

        int commonSuffixLength = findCommonSuffixLength(str1, str2, commonPrefixLength);

        str1 = str1.subSequence(commonPrefixLength, str1.length() - commonSuffixLength);
        str2 = str2.subSequence(commonPrefixLength, str2.length() - commonSuffixLength);

        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                        : 1));
            }
        }
        
        int dist = distance[str1.length()][str2.length()];

        //putInCache(str1, str2, dist);

        return dist;
    }
}
