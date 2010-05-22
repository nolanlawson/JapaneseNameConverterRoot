package com.nolanlawson.japanesenamegenerator.v3.pronunciation;

import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author nolan
 */
public class PronunciationGuide {

    private static String[] geminateConsonants = new String[]{"bb", "cch", "dd", "ff", "gg", "jj",
    "kk", "pp", "ss", "tt", "tch"};
    private static Map<String,String> guideHash = getPronuncationGuideHash();

    /**
     * Return a pronunciation guide, where each Pair is a pair of:
     * 1) a substring in the original roomaji
     * 2) an explanation of how to pronounce it
     * 
     * @param roomaji
     * @return
     */
    public static List<Pair<String,String>> getPronunciationGuide(String roomaji) {

        List<Pair<String,String>> result = new ArrayList<Pair<String, String>>();

        for (int i = 0; i < roomaji.length(); i++) {
            if (i < roomaji.length() - 2) {
                String trigram = roomaji.substring(i, i + 3);
                if (guideHash.containsKey(trigram)) {
                    result.add(new Pair<String,String>(trigram, guideHash.get(trigram)));
                    i += 2;
                    continue;
                }
            }



            if (i < roomaji.length() - 1) {
                String bigram = roomaji.substring(i, i + 2);
                if (guideHash.containsKey(bigram)) {
                    result.add(new Pair<String,String>(bigram, guideHash.get(bigram)));
                    i++;
                    continue;
                }
            }
            String unigram = Character.toString(roomaji.charAt(i));
            if (guideHash.containsKey(unigram)) {
                result.add(new Pair(unigram, guideHash.get(unigram)));
            }
        }

        // delete duplicates
        Set<Pair<String,String>> resultSet = new HashSet<Pair<String, String>>();
        for (int i = 0; i < result.size(); i++) {
            if (resultSet.contains(result.get(i))) {
                result.remove(i);
                i--;
            } else {
                resultSet.add(result.get(i));
            }
        }

        return result;

    }

    private static Map<String, String> getPronuncationGuideHash() {
        Map<String,String> result = new HashMap<String, String>(){{

            put("a", "like the \"a\" in \"father\"");
            put("o", "like the \"o\" in \"orange\"");
            put("u", "like  the \"u\" in \"hula\"");
            put("i", "like the \"ee\" in \"bee\"");
            put("e", "like the \"ey\" in \"hey\"");

            put("aa", "like the \"a\" in \"father,\" but held twice as long");
            put("oo", "like the \"o\" in \"orange,\" but held twice as long");
            put("ou", "like the \"o\" in \"orange,\" but held twice as long");
            put("uu", "like  the \"u\" in \"hula,\" but held twice as long");
            put("ii", "like the \"ee\" in \"bee,\" but held twice as long");
            put("ee", "like the \"ey\" in \"hey,\" but held twice as long");
            put("ei", "like the \"ey\" in \"hey,\" but held twice as long");

            put("oi", "like the  \"oy\" in \"boy\"");
            put("ai", "like the  \"ai\" in \"samurai\"");

            put("r", "pronounced similar to the \"tt\" in \"kitty\" (American accent only), or the Spanish \"r\" as in \"pero\" (dog)");

            put("t", "like the  \"t\" in \"tea\"; never like the \"t\" in \"petal\" (in American accents)");
            put("d", "like the  \"d\" in \"day\"; never like the \"d\" in \"pedal\" (in American accents)");


        }};

        String geminateConsonantExplanation = "Double consonants are pronounced with a short pause preceding the consonant";

        for (String geminateConsonant : geminateConsonants) {
            if (geminateConsonant.equals("tt")) {
                result.put(geminateConsonant, result.get("t") +". " + geminateConsonantExplanation);
            } else if (geminateConsonant.equals("dd")) {
                result.put(geminateConsonant, result.get("d") +". " + geminateConsonantExplanation);
            } else {
                result.put(geminateConsonant, geminateConsonantExplanation);
            }
        }

        return result;
    }

}
