package com.nolanlawson.japanesenamegenerator.v3.katakana;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nolan
 */
public class RomaajiMassager {

    //special case for syllabic 'm' before bilabial consonants
    private static Pattern mPattern1 = Pattern.compile("(m)([pbfw])");

    //consonants who usually are followed by 'u' when transliterated
    private static Pattern shPattern = Pattern.compile("(sh)([^aioeuy]|$)");
    private static Pattern tsPattern = Pattern.compile("(ts)([^aioeu]|$)");
    private static Pattern sPattern = Pattern.compile("(s)([^aeiouyhs]|$)");

    private static Pattern bPattern = Pattern.compile("(b)([^aeiouyb]|$)");
    private static Pattern fPattern = Pattern.compile("(f)([^aeiouf]|$)");
    private static Pattern gPattern = Pattern.compile("(g)([^aeiouyg]|$)");
    private static Pattern kPattern = Pattern.compile("(k)([^aeiouyk]|$)");
    private static Pattern mPattern2 = Pattern.compile("(m)([^aeiouy]|$)");
    private static Pattern pPattern = Pattern.compile("(p)([^aeiouyp]|$)");
    private static Pattern rPattern = Pattern.compile("(r)([^aeiouy]|$)");
    private static Pattern zPattern = Pattern.compile("(z)([^aeiouz]|$)");

    //consonants who usually are followed by 'o' when transliterated
    private static Pattern tPattern = Pattern.compile("(t)([^aeiouyst]|ch|$)");
    private static Pattern dPattern = Pattern.compile("(d)([^aeiouyd]|$)");
    private static Pattern hPattern = Pattern.compile("(h)([^aeiouy]|$)");

    //consonants who usually are followed by 'i' when transliterated
    private static Pattern chPattern = Pattern.compile("(ch)([^aeiouy]|$)");
    private static Pattern jPattern = Pattern.compile("(j)([^aeiouyj]|$)");

    // glides in the wrong place
    private static Pattern wPattern = Pattern.compile("(w)([^aeiou]|$)");
    private static Pattern yPattern = Pattern.compile("(y)([^aeiou]|$)");

    // double n's not followed by a vowel

    private static Pattern nnPattern = Pattern.compile("(nn)([^aeiouy]|$)");

    /**
     * Attempts to correct malformed romaaji by adding vowels.
     * @param romaaji
     * @return
     */
    public String massageMalformedRomaaji(String romaaji) {

        if (romaaji == null || romaaji.equals("")) {
            return romaaji;
        }

        for (Pattern pattern : new Pattern[]{mPattern1}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("n$2");
        }

        for (Pattern pattern : new Pattern[]{shPattern, tsPattern, sPattern, bPattern, fPattern, gPattern,
                                             kPattern, mPattern2, pPattern, rPattern, zPattern, yPattern}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("$1u$2");
        }
        
        for (Pattern pattern : new Pattern[]{tPattern,dPattern,hPattern}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("$1o$2");
        }        
        
        for (Pattern pattern : new Pattern[]{chPattern,jPattern}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("$1i$2");
        }        

        for (Pattern pattern : new Pattern[]{wPattern}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("$2");
        }

        for (Pattern pattern : new Pattern[]{nnPattern}) {
            Matcher matcher = pattern.matcher(romaaji);
            romaaji = matcher.replaceAll("n$2");
        }
        
        return romaaji;
    }
}
