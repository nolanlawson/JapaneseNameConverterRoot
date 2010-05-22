package com.nolanlawson.japanesenamegenerator.v3.kanji;

import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author nolan
 */
public class KanjiGenerator {

    private static Pattern letterPattern = Pattern.compile("[a-zA-Z]");

    private Map<String,List<String>> roomajiToKanjiMap;
    private Map<String,List<String>> kanjiToEnglishMap;

    private static List<RoomajiTransformer> allRoomajiTransformers = getRoomajiTransformers();

    /**
     * create a new KanjiGenerator from the csv file's fileinputstream
     * @param kanjiDictionaryFileInputStream
     */
    public KanjiGenerator(InputStream kanjiDictionaryFileInputStream) {


        readInDictionary(kanjiDictionaryFileInputStream);


    }
    
    /**
     * returns a list of list of kanji results, where each entry in the first list
     * is a syllable and each entry in the second list is an alternative kanjiresult
     * for that syllable.  Each kanjiresult contains the roomaji,kanji,and english meaning.
     *
     * @param roomaji
     * @return
     */
    public List<List<KanjiResult>> generateKanji(String roomaji) {
        roomaji = roomaji.trim().toLowerCase();
        roomaji = normalizeRoomaji(roomaji);

        String[] tokens = StringUtil.quickSplit(roomaji, " ");

        List<KanjiResult> result = new ArrayList<KanjiResult>();

        // break up by tokens in case there's a first name + last name
        for (String token : tokens) {
            List<KanjiResult> subResult = findExactMatchKanjiSequenceForToken(token);
            result.addAll(subResult);
        }

        return expandKanjiResults(result);

    }

    private void cleanKanjiResults(List<KanjiResult> kanjiResults) {

        // this is basicaly just to fix those situations where there are a lot of spurious
        // extra 'a's and 'i's and such which compensated for long vowels

        for (int i = 1; i < kanjiResults.size(); i++) {
            KanjiResult kanjiResult = kanjiResults.get(i);

            String roomaji = kanjiResult.getRoomaji();
            if (roomaji.length() == 1 && StringUtil.isVowel(roomaji.charAt(0))) {
                if (kanjiResults.get(i - 1).getRoomaji().endsWith(roomaji)) {
                    // double vowel - get rid of it
                    kanjiResults.remove(i);
                    i--;
                }
            }

        }


    }

    private List<List<KanjiResult>> expandKanjiResults(List<KanjiResult> kanjiResults) {
        // add in additional kanji and meanings - expanding into a 2d array in the process

        List<List<KanjiResult>> expandedKanjiResultLists = new ArrayList<List<KanjiResult>>();

        for (KanjiResult kanjiResult : kanjiResults) {
            List<KanjiResult> expandedResults = new ArrayList<KanjiResult>();

            for (String kanji : roomajiToKanjiMap.get(kanjiResult.getRoomaji())) {
                String english = kanjiToEnglishMap.get(kanji).get(0); // just get the first english meaning

                KanjiResult newResult = new KanjiResult();
                newResult.setKanji(kanji);
                newResult.setRoomaji(kanjiResult.getRoomaji());
                newResult.setEnglish(english);

                expandedResults.add(newResult);
            }

            expandedKanjiResultLists.add(expandedResults);

        }

        return expandedKanjiResultLists;
    }

    private List<KanjiResult> findExactMatchKanjiSequenceForToken(String roomaji) {
        List<KanjiResult> result = findExactMatchKanjiSequences(roomaji);


        if (result == null) {
            List<RoomajiTransformer> transformers = getPossibleTransformers(roomaji);

            for (int i = 0; i < transformers.size() && result == null; i++) {
                RoomajiTransformer transformer = transformers.get(i);
                roomaji = transformer.apply(roomaji);
                result = findExactMatchKanjiSequences(roomaji);
            }
        }

        if (result == null) {
            return Collections.emptyList();
        }

        cleanKanjiResults(result);

        return result;
    }

    private List<KanjiResult> findExactMatchKanjiSequences(String roomaji) {

        // roomaji broken up into viable substrings, e.g. [noo,ran]
        List<List<String>> substringSequences = findExactMatchSubstringSequences(roomaji);

        for (List<String> substringSequence : substringSequences) {
            List<KanjiResult> kanjiAndEnglishStrings = new ArrayList<KanjiResult>();
            
            for (String substring : substringSequence) {
                String kanji = roomajiToKanjiMap.get(substring).get(0);
                String english = kanjiToEnglishMap.get(kanji).get(0);

                KanjiResult kanjiResult = new KanjiResult();
                kanjiResult.setKanji(kanji);
                kanjiResult.setEnglish(english);
                kanjiResult.setRoomaji(substring);

                kanjiAndEnglishStrings.add(kanjiResult);
            }
            return kanjiAndEnglishStrings;
            // just choose the first one.  It's usually the best (based on experience)
        }

        return null;

    }

    private List<List<String>> findExactMatchSubstringSequences(String roomaji) {
        List<List<SubstringNode>> substringSequences = new ArrayList<List<SubstringNode>>();

        // add initial first guesses at possible substring nodes
        substringSequences.add(new ArrayList<SubstringNode>());
        int limit = Math.min(4, roomaji.length());
        for (int i = 1; i <= limit; i++) {
            String substring = roomaji.substring(0,i);
            if (roomajiToKanjiMap.containsKey(substring)) {
                SubstringNode substringNode = new SubstringNode();
                substringNode.setStr(substring);
                substringNode.setStartIndex(0);
                substringNode.setEndIndex(i);
                substringSequences.get(0).add(substringNode);
            }
        }

        if (substringSequences.get(0).isEmpty()) {
            return Collections.emptyList(); // no possible answer
        }

        for (int i = 1;;i++) {

            List<SubstringNode> currentSubtringNodes =  new ArrayList<SubstringNode>();

            for (SubstringNode previousSubstringNode : substringSequences.get(i - 1)) {
                int previousEndIndex = previousSubstringNode.getEndIndex();
                int maxEndIndex = Math.min(previousEndIndex + 4, roomaji.length());
                for (int j = previousEndIndex + 1; j <= maxEndIndex; j++) {
                    String substring = roomaji.substring(previousEndIndex,j);
                    if (roomajiToKanjiMap.containsKey(substring)) {
                        SubstringNode substringNode = new SubstringNode();
                        substringNode.setPrevious(previousSubstringNode);
                        substringNode.setStr(substring);
                        substringNode.setStartIndex(previousEndIndex);
                        substringNode.setEndIndex(j);
                        currentSubtringNodes.add(substringNode);
                    }
                }
            }
            
            if (currentSubtringNodes.isEmpty()) {
                break;
            }
            substringSequences.add(currentSubtringNodes);
        }

        List<List<String>> result = new ArrayList<List<String>>();

        // build up the substrings sequences backwards
        for (List<SubstringNode> substringSequence : substringSequences) {
            for (SubstringNode finalSubstringNode : substringSequence) {

                if (finalSubstringNode.getEndIndex() != roomaji.length()) { // this substring sequence doesn't reach to the end
                    continue;
                }

                List<String> substringSequenceAsStringList = new ArrayList<String>();

                SubstringNode currentSubstringNode = finalSubstringNode;
                while (currentSubstringNode != null) {
                    substringSequenceAsStringList.add(currentSubstringNode.getStr());
                    currentSubstringNode = currentSubstringNode.getPrevious();
                }
                Collections.reverse(substringSequenceAsStringList);

                result.add(substringSequenceAsStringList);
            }
        }

        return result;
    }

    private static List<RoomajiTransformer> getRoomajiTransformers() {

        List<RoomajiTransformer> result = new ArrayList<RoomajiTransformer>();

        // shortening 'i' vowel
        result.add(new ReplacementRoomajiTransformer("ii", "i"));

        // 'she' -> 'shi'
        result.add(new ReplacementRoomajiTransformer("she", "shi"));

        // 'ti' -> 'chi'
        result.add(new ReplacementRoomajiTransformer("ti", "chi"));

        // 'tu' -> 'chu'
        result.add(new ReplacementRoomajiTransformer("tu", "chu"));

        // 'du' -> 'ju'
        result.add(new ReplacementRoomajiTransformer("du", "ju"));

        // 'di' -> 'ji'
        result.add(new ReplacementRoomajiTransformer("di", "ji"));

        // 'che' -> 'chi'
        result.add(new ReplacementRoomajiTransformer("che", "chi"));

        // 'je' -> 'ji'
        result.add(new ReplacementRoomajiTransformer("je", "ji"));

        // 'we' -> 'ue'
        result.add(new ReplacementRoomajiTransformer("we", "ue"));

        // 'wi' -> 'ui'
        result.add(new ReplacementRoomajiTransformer("wi", "ui"));

        // 'wo' -> 'o'
        result.add(new ReplacementRoomajiTransformer("wo", "o"));

        // 'wu' -> 'u'
        result.add(new ReplacementRoomajiTransformer("wu", "u"));

        // 'ye' -> 'e'
        result.add(new ReplacementRoomajiTransformer("ye", "e"));

        // 'yi' -> 'i'
        result.add(new ReplacementRoomajiTransformer("yi", "i"));

        // 'fe' -> 'he'
        result.add(new ReplacementRoomajiTransformer("fe", "he"));

        // 'fo' -> 'ho'
        result.add(new ReplacementRoomajiTransformer("fo", "ho"));

        // 'fa' -> 'ha'
        result.add(new ReplacementRoomajiTransformer("fa", "ha"));

        // 'fi' -> 'hi'
        result.add(new ReplacementRoomajiTransformer("fi", "hi"));

        // getting rid of geminate consonants
        result.add(new ReplacementRoomajiTransformer("'", ""));
        
        // replacing 'p' with 'b'
        result.add(new ReplacementRoomajiTransformer("p", "b"));

        // lengthening 'e' vowel when NOT before 'n'
        result.add(new RegexRoomajiTransformer("([^e]|^)e([^en]|$)", "$1ee$2"));
        
        // lengthening 'o' vowel when NOT before 'n'
        result.add(new RegexRoomajiTransformer("([^o]|^)o([^on]|$)", "$1oo$2"));

        // lengthening 'u' vowel when NOT before 'n'
        result.add(new RegexRoomajiTransformer("([^u]|^)u([^un]|$)", "$1uu$2"));

        // lengthening 'e' vowel
        result.add(new RegexRoomajiTransformer("([^e]|^)e([^e]|$)", "$1ee$2"));
        
        // lengthening 'o' vowel
        result.add(new RegexRoomajiTransformer("([^o]|^)o([^o]|$)", "$1oo$2"));

        // lengthening 'u' vowel
        result.add(new RegexRoomajiTransformer("([^u]|^)u([^u]|$)", "$1uu$2"));

        // remove palatalization
        result.add(new ReplacementRoomajiTransformer("gy", "g"));
        result.add(new ReplacementRoomajiTransformer("ky", "k"));
        result.add(new ReplacementRoomajiTransformer("hy", "h"));
        result.add(new ReplacementRoomajiTransformer("py", "p"));
        result.add(new ReplacementRoomajiTransformer("by", "b"));
        result.add(new ReplacementRoomajiTransformer("my", "m"));
        result.add(new ReplacementRoomajiTransformer("ny", "n"));
        result.add(new ReplacementRoomajiTransformer("ry", "r"));
        result.add(new ReplacementRoomajiTransformer("ky", "k"));

        // turning final 'n' into 'nu'
        result.add(new RegexRoomajiTransformer("n([^aeiouy]|$)", "nu$1"));

        return result;
    }

    private String normalizeRoomaji(String roomaji) {
        // turn geminate consonants into apostrophe + consonant to make lookups easier

        roomaji = StringUtil.quickReplace(roomaji, "cc", "'c");
        roomaji = StringUtil.quickReplace(roomaji, "kk", "'k");
        roomaji = StringUtil.quickReplace(roomaji, "pp", "'p");
        roomaji = StringUtil.quickReplace(roomaji, "ss", "'s");
        roomaji = StringUtil.quickReplace(roomaji, "tt", "'t");
        roomaji = StringUtil.quickReplace(roomaji, "dd", "'d");
        roomaji = StringUtil.quickReplace(roomaji, "bb", "'b");

        return roomaji;
    }

    private void readInDictionary(InputStream kanjiDictionaryFileInputStream) {

        roomajiToKanjiMap = new HashMap<String, List<String>>();
        kanjiToEnglishMap = new HashMap<String, List<String>>();

        try {
            BufferedReader buff = new BufferedReader(new InputStreamReader(kanjiDictionaryFileInputStream));

            if (buff.ready()) {
                buff.readLine(); // throw away first line of csv file
            }
            while (buff.ready()) {
                String line = buff.readLine();

                String[] splitLine = StringUtil.quickSplit(line, "\t");

                if (splitLine.length < 3 || splitLine[2].length() == 0) {
                    continue; // some kanji don't have on'yomi readings
                }

                String kanji = splitLine[0];
                String english = splitLine[1];
                String roomaji = splitLine[2];

                // get rid of surrounding quotes in csv
                kanji = kanji.substring(1,kanji.length() - 1);
                english = english.substring(1,english.length() - 1);
                roomaji = roomaji.substring(1,roomaji.length() - 1);

                String[] englishStrings = StringUtil.quickSplit(english, ",");
                String[] roomajiStringArray = StringUtil.quickSplit(roomaji, ",");

                Set<String> roomajiStrings = new HashSet<String>();
                for (String roomajiString : roomajiStringArray) {

                    if (!letterPattern.matcher(roomajiString).find()) {
                        continue; // there are some katakana representations in that file, so skip 'em
                    }
                    
                    roomajiString = roomajiString.trim();

                    roomajiStrings.add(roomajiString);
                    if (roomajiString.endsWith("tsu") && roomajiString.length() > 3) {
                        roomajiStrings.add(StringUtil.quickReplace(roomajiString, "tsu", "'"));
                    }
                }

                List<String> englishStringList = null;

                if (englishStrings.length > 1) {
                    englishStringList = new ArrayList<String>();

                    for (String englishString : englishStrings) {
                        englishStringList.add(englishString.trim());
                    }
                } else if (englishStrings.length == 1) {
                    englishStringList = Collections.singletonList(englishStrings[0].trim());
                }
                kanjiToEnglishMap.put(kanji,englishStringList);

                for (String roomajiString : roomajiStrings) {

                    List<String> existingKanjis = roomajiToKanjiMap.get(roomajiString);
                    if (existingKanjis == null) {
                        existingKanjis = new ArrayList<String>();
                        roomajiToKanjiMap.put(roomajiString, existingKanjis);
                    }
                    existingKanjis.add(kanji);
                }
            }
            buff.close();

        } catch (Throwable t) {
            throw new RuntimeException("Failed to read in file: " + kanjiDictionaryFileInputStream,t);
        }
    }

    /**
     * Cheat a bit with the roomaji representation, so that we can produce at least SOME kanji version.
     * Tries to cheat as little as possible by making changes such as e.g. lengthening vowels or
     * removing geminate consonants.
     *
     * @param roomaji
     * @param i
     * @return
     */
    private List<RoomajiTransformer> getPossibleTransformers(String roomaji) {
        List<RoomajiTransformer> result = new ArrayList<RoomajiTransformer>();

        for (RoomajiTransformer roomajiTransformer : allRoomajiTransformers) {
            if (roomajiTransformer.appliesToString(roomaji)) {
                result.add(roomajiTransformer);
            }
        }

        return result;
    }

    // serialize out the kanji dictionaries
    public static void main(String[] args) throws Exception {
        
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("kanji_dictionary.csv");
        KanjiGenerator kg = new KanjiGenerator(inputStream);

        System.out.println(kg.generateKanji("ramoona"));
    }
}
