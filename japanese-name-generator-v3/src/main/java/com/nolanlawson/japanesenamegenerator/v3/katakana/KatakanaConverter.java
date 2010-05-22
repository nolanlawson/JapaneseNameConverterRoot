package com.nolanlawson.japanesenamegenerator.v3.katakana;

import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nolan
 */
public class KatakanaConverter {

    private static Pattern invalidInputChars = Pattern.compile("[^a-z']");

    public String convertToKatakana(String romaaji) throws KatakanaParseException {

        String originalRomaaji = romaaji;

        if (romaaji == null) {
            throw new IllegalArgumentException("Null string: " + originalRomaaji);
        }

        Matcher matcher = invalidInputChars.matcher(romaaji.toLowerCase().trim());
        
        romaaji = matcher.replaceAll("");

        if (romaaji.equals("")) {
            throw new IllegalArgumentException("String has no valid characters: '" + originalRomaaji+"'");
        }

        StringBuffer sb = new StringBuffer(romaaji);

        int i = 0;
        while (i < sb.length()) {
            char ch = sb.charAt(i);

            String replacementString;
            int replacedStringSize;
            // special case for syllabic 'n' followed by consonant or '\''
            if (ch == 'n' && i < sb.length() - 1
                         && (StringUtil.isConsonant(sb.charAt(i + 1)) || sb.charAt(i + 1)=='\'')) {
                replacementString = convertN(ch);
                if (sb.charAt(i + 1) == '\'') {
                    replacedStringSize = 2;
                } else {
                    replacedStringSize = 1;
                }
            // special case for syllabic 'n' at end of string
            } else if (ch == 'n' && i == sb.length() -1) {
                replacementString = convertN(ch);
                replacedStringSize = 1;

            // double consonant that doesn't involve a small 'tsu'
            } else if (
                    //'sh' or 'ch'
                    (ch == 's' || ch == 'c') && i < sb.length() - 1
                    && sb.charAt(i + 1) == 'h' && i < sb.length() - 2
                    && StringUtil.isVowel(sb.charAt(i + 2))
                    ||
                    // palatalized double consonant, such as 'ry' or 'ky'
                    (StringUtil.isConsonant(ch) && i < sb.length() - 1 && sb.charAt(i + 1) == 'y'
                    && i < sb.length() - 2 && StringUtil.isVowel(sb.charAt(i + 2)))
                    ||
                    // 'ts'
                    (ch == 't' && i < sb.length() - 1 && sb.charAt(i + 1)=='s')
                    ) {
                
                // double consonant followed by double vowel, e.g. shii
                if (i < sb.length() - 3 && StringUtil.isVowel(sb.charAt(i + 3))) {
                    replacementString = convertConsonantConsonantVowelVowel(ch, sb.charAt(i + 1),
                            sb.charAt(i + 2), sb.charAt(i + 3));
                    replacedStringSize = 4;
                // double consonant followed by single vowel, e.g. shi
                } else {
                    replacementString = convertConsonantConsonantVowel(ch, sb.charAt(i + 1), sb.charAt(i + 2));
                    replacedStringSize = 3;
                }

            // true geminated consonant
            } else if (ch != 'n' && StringUtil.isConsonant(ch) && i < sb.length() - 1
                    && StringUtil.isConsonant(sb.charAt(i + 1))) {
                replacementString = convertSmallTsu();
                replacedStringSize = 1;

                // single consonant followed by vowel
            } else if ((StringUtil.isConsonant(ch) || ch == 'y') && i < sb.length() - 1 && StringUtil.isVowel(sb.charAt(i + 1))) {
                
                // consonant followed by double vowel
                if (i < sb.length() - 2 && StringUtil.isVowel(sb.charAt(i + 2))) {

                    replacementString = convertConsonantVowelVowel(ch, sb.charAt(i + 1), sb.charAt(i + 2));
                    replacedStringSize = 3;


                    // no double vowels found
                    if (replacementString.indexOf(Katakana.PROLONGED_SOUND_MARK.getChar()) == -1) {
                        // need to reconsider the second vowel
                        replacementString = replacementString.substring(0, replacementString.length() - 1);
                        replacedStringSize = 2;
                    }

                // consonant followed by single vowel
                } else {
                    
                    replacementString = convertConsonantVowel(ch, sb.charAt(i + 1));
                    replacedStringSize = 2;
                }
            } else if (StringUtil.isVowel(ch)) {

                // geminate vowel
                if (i < sb.length() - 1 && StringUtil.isVowel(sb.charAt(i + 1))) {

                    replacementString = convertVowelVowel(ch, sb.charAt(i + 1));
                    replacedStringSize = 2;

                    // no double vowels found
                    if (replacementString.indexOf(Katakana.PROLONGED_SOUND_MARK.getChar()) == -1) {
                        // need to reconsider the second vowel
                        replacementString = replacementString.substring(0, replacementString.length() - 1);
                        replacedStringSize = 1;
                    }

               // single vowel
                } else {

                    replacementString = convertVowel(ch);
                    replacedStringSize = 1;
                }

            } else {
                throw new KatakanaParseException(romaaji, i);
            }

            sb.replace(i, i + replacedStringSize, replacementString);

            i += replacementString.length();

        }

        String result = sb.toString();

        for (int j = 0; j < result.length(); j++) {
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(result.charAt(j));
            if (unicodeBlock != Character.UnicodeBlock.KATAKANA
                    && unicodeBlock != Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) {
                throw new KatakanaParseException(romaaji, j);
            }
        }

        return sb.toString();

    }

    private String convertConsonantConsonantVowelVowel(char consonant1, char consonant2, char vowel1, char vowel2) {

        String convertedFirstThreeChars = convertConsonantConsonantVowel(consonant1, consonant2, vowel1);


        String convertedVowel2 = Character.toString(convertVowelVowel(vowel1, vowel2).charAt(1));

        return new StringBuilder(convertedFirstThreeChars).append(convertedVowel2).toString();
    }

    private String convertConsonantVowelVowel(char consonant, char vowel1, char vowel2) {

        String convertedFirstTwoChars = convertConsonantVowel(consonant, vowel1);

        String convertedVowel2 = Character.toString(convertVowelVowel(vowel1, vowel2).charAt(1));

        return new StringBuilder(convertedFirstTwoChars).append(convertedVowel2).toString();
    }

    private String convertVowelVowel(char vowel1, char vowel2) {

        // check for any lengthened vowels
        switch (vowel1) {
            case 'i':
                if (vowel2 == 'i') {
                    return convertLengthenedVowel(vowel1);
                }
                break;
            case 'e':
                if (vowel2 == 'e' || vowel2 == 'i') {
                    return convertLengthenedVowel(vowel1);
                }
                break;
            case 'a':
                if (vowel2 == 'a') {
                    return convertLengthenedVowel(vowel1);
                }
                break;
            case 'o':
                if (vowel2 == 'o' || vowel2 == 'u') {
                    return convertLengthenedVowel(vowel1);
                }
                break;
            case 'u':
                if (vowel2 == 'u') {
                    return convertLengthenedVowel(vowel1);
                }
                break;
        }

        // otherwise it's just a garden-variety diphthong
        return convertVowel(vowel1) + convertVowel(vowel2);
    }

    private String convertLengthenedVowel(char vowel) {
        return Katakana.valueOf(Character.toString(Character.toUpperCase(vowel))).getString()
                + Katakana.PROLONGED_SOUND_MARK.getString();
    }

    private String convertVowel(char vowel) {
        return Katakana.valueOf(Character.toString(Character.toUpperCase(vowel))).getString();
    }

    private String convertConsonantVowel(char consonant, char vowel) {

        // special 'fa/fe/fi/fo' case
        if (consonant == 'f' && vowel != 'u') {
            return StringUtil.concat(Katakana.FU.getChar(), getSmallVowel(vowel));
        // special 'du' case
        } else if (consonant == 'd' && vowel == 'u') {
            return StringUtil.concat(Katakana.DO.getChar(), getSmallVowel(vowel));
        // special 'di' case
        } else if (consonant == 'd' && vowel == 'i') {
            return StringUtil.concat(Katakana.DE.getChar(), getSmallVowel(vowel));
        // speical 'tu' case
        } else if (consonant == 't' && vowel == 'u') {
            return StringUtil.concat(Katakana.TO.getChar(), getSmallVowel(vowel));
        // speical 'tu' case
        } else if (consonant == 't' && vowel == 'i') {
            return StringUtil.concat(Katakana.TE.getChar(), getSmallVowel(vowel));
        // special 'ja/je/ji/jo/ju' case
        } else if (consonant == 'j') {
            if (vowel == 'e') {
                return StringUtil.concat(Katakana.JI.getChar(), Katakana.SMALL_E.getChar());
            } else if (vowel == 'i') {
                return Katakana.JI.getString();
            } else {
                return StringUtil.concat(Katakana.JI.getChar(), getSmallPalatalizedVowel(vowel));
            }
        // 'ye' doesn't actually exist, but we'll be lenient
        } else if (consonant == 'y' && vowel == 'e') {
            return StringUtil.concat(Katakana.I.getChar(),Katakana.SMALL_E.getChar());
        // ditto for 'yi'
        } else if (consonant == 'y' && vowel == 'i') {
            return Katakana.I.getString();
        // special 'w' case
        } else if (consonant == 'w') {
            if (vowel == 'u') {
                return Katakana.U.getString();
            } else if (vowel == 'a') {
                return Katakana.WA.getString();
            } else {
                return StringUtil.concat(Katakana.U.getChar(), getSmallVowel(vowel));
            }
        }

        return Katakana.valueOf(StringUtil.concat(consonant, vowel).toUpperCase()).getString();
    }

    private String convertN(char n) {
        return Katakana.N.getString();
    }

    private String convertConsonantConsonantVowel(char consonant1, char consonant2, char vowel) {

        String convertedConsonants;

        // true if this is a palatal consonant - i.e. one where the 'y' or 'i' is implicit
        boolean palatalized = true;

        if (consonant2 == 'y') {
            // 'ry','ky','by',etc.
            convertedConsonants =
                    Katakana.valueOf(StringUtil.concat(consonant1, 'i').toUpperCase()).getString();
        } else if (consonant1 == 't' && consonant2 == 's') {
            // 'ts'
            convertedConsonants = Katakana.TSU.getString();
            palatalized = false;
        } else {
            // 'ch','sh'
            convertedConsonants =
                Katakana.valueOf(StringUtil.concat(consonant1, consonant2, 'i').toUpperCase()).getString();
        }
        
        String convertedVowel;

        switch (vowel) {
            
            case 'i':
                convertedVowel = palatalized ? "" : Katakana.SMALL_I.getString();
                break;
            
            case 'e':
                convertedVowel = Katakana.SMALL_E.getString();
                break;
            
            case 'a':
                convertedVowel = palatalized ? Katakana.SMALL_YA.getString() : Katakana.SMALL_A.getString();
                break;
            
            case 'o':
                convertedVowel = palatalized ? Katakana.SMALL_YO.getString() : Katakana.SMALL_O.getString();
                break;
            
            default: //case 'u':
                convertedVowel = palatalized ? Katakana.SMALL_YU.getString() : "";
                break;
        }

        return convertedConsonants + convertedVowel;
    }

    private String convertSmallTsu() {
        return Katakana.SMALL_TU.getString();
    }

    private char getSmallVowel(char vowel) {
        return Katakana.valueOf("SMALL_" + Character.toUpperCase(vowel)).getChar();
    }

    private char getSmallPalatalizedVowel(char vowel) {
        return Katakana.valueOf("SMALL_Y" + Character.toUpperCase(vowel)).getChar();
    }

}
