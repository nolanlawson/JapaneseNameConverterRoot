package com.nolanlawson.japanesenamegenerator.v3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nolanlawson.japanesenamegenerator.v3.data.Model;
import com.nolanlawson.japanesenamegenerator.v3.data.ModelMarshaller;
import com.nolanlawson.japanesenamegenerator.v3.katakana.KatakanaConverter;
import com.nolanlawson.japanesenamegenerator.v3.katakana.KatakanaParseException;
import com.nolanlawson.japanesenamegenerator.v3.katakana.RomaajiMassager;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;

/**
 *
 * @author nolan
 */
public class JapaneseNameGenerator {

    private static final String ROMAAJI_MODEL_FILENAME = "romaaji_model_20090125_all1_2_2_min2_truncated2.txt";
    private static final String DIRECT_LOOKUP_FILENAME = "all_names.txt";

    private static Map<Character,String> lettersInJapanese = new HashMap<Character, String>(){{
            put('a',"ee");
            put('b',"bii");
            put('c',"shii");
            put('d',"dii");
            put('e',"ii");
            put('f',"efu");
            put('g',"jii");
            put('h',"ecchi");
            put('i',"ai");
            put('j',"jee");
            put('k',"kee");
            put('l',"eru");
            put('m',"emu");
            put('n',"enu");
            put('o',"oo");
            put('p',"pii");
            put('q',"kyuu");
            put('r',"aaru");
            put('s',"esu");
            put('t',"tii");
            put('u',"yuu");
            put('v',"bui");
            put('w',"daburu");
            put('x',"ekkusu");
            put('y',"wai");
            put('z',"zetto");
    }};

    // two consecutive consonants (e.g. 'jt' or 'dj') or an initials pattern with periods
    private static Pattern initialsPattern = Pattern.compile("([bcdfghjklmnpqrstvwxyz]{2}|([a-z]\\.){1,2})");
    private static Pattern nonlettersPattern = Pattern.compile("[^a-zA-Z]");

    private Model model;
    private Map<String, String> directLookupNames;
    private KatakanaConverter katakanaConverter = new KatakanaConverter();
    private RomaajiMassager romaajiMassager = new RomaajiMassager();

    public JapaneseNameGenerator() {
        this.model = ModelMarshaller.readFromSystemResource(ROMAAJI_MODEL_FILENAME);
        this.directLookupNames = loadDirectLookupNames();
    }

    /**
     * You must use this constructor if you're using this project as an extrnal jar in
     * an Android app - otherwise, it can't find the resources in the jar
     * @param romaajiModelInputStream - txt file version of the romaaji model
     * @param directLookupNamesInputStream - txt file of common english name/romaaji name mappings
     */
    public JapaneseNameGenerator(InputStream romaajiModelInputStream,
            InputStream directLookupNamesInputStream) {
        
        this.model = ModelMarshaller.readFromInputStream(romaajiModelInputStream);
        this.directLookupNames = loadDirectLookupNamesFromInputStream(directLookupNamesInputStream);
    }


    /**
     * Returns a romaaji representation of the english name and a katakana representation
     * @param english
     * @return
     * @throws com.nolanlawson.japanesenamegenerator.v3.ConversionException
     */
    public Pair<String,String> convertToRomaajiAndKatakana(String english) throws ConversionException {

        if (english == null || english.trim().equals("")) {
            throw new ConversionException("String is null or empty");
        }
        
        english = english.trim().toLowerCase();

        String[] tokens = StringUtil.quickSplit(english, " ");
        
        StringBuilder romaaji = new StringBuilder();
        StringBuilder katakana = new StringBuilder();

        
        for (String token : tokens) {
            
            Pair<String,String> convertedPair = convertEnglishToken(token);
            romaaji.append(convertedPair.getFirst()).append(" ");
            katakana.append(convertedPair.getSecond()).append(" ");
        }
        
        // cut off last space
        String katakanaResult = katakana.substring(0, katakana.length() - 1);
        String romaajiResult = romaaji.substring(0, romaaji.length() - 1);

        return Pair.create(romaajiResult, katakanaResult);

    }

    private Pair<String,String> convertEnglishToken(String token) throws ConversionException {

        String romaaji;

        Matcher matcher = initialsPattern.matcher(token);

        // first try an initials match
        if (matcher.matches()) {
            romaaji = convertInitials(token);
        // then try direct lookup
        } else if (directLookupNames.containsKey(token)) {
            romaaji = directLookupNames.get(token);
        // then try transliteration using the model
        } else {
            token = nonlettersPattern.matcher(token).replaceAll(""); // replace non-letters with ""
            // XXX HACK: as of 20100228, the model screws up on ending 'th', so this is my hacky solution - replace with 's'
            if (token.endsWith("th") && token.length() > 3) {
                token = token.substring(0, token.length() - 2) + "s";
            }
            romaaji = model.transformString(token);
        }

        romaaji = romaajiMassager.massageMalformedRomaaji(romaaji);
        
        romaaji = normalizeRoomaji(romaaji);

        String katakana;

        try {
            katakana = katakanaConverter.convertToKatakana(romaaji);
        } catch (KatakanaParseException ex) {
            throw new ConversionException("Could not parse romaaji: '" + romaaji+"'", ex);
            
        }

        return Pair.create(romaaji, katakana);
    }

    private String convertInitials(String token) {

        StringBuilder sb = new StringBuilder();

        for (char ch : token.toCharArray()) {
            if (lettersInJapanese.containsKey(ch)) {
                sb.append(lettersInJapanese.get(ch));
            }
        }

        return sb.toString();
    }

    private Map<String, String> loadDirectLookupNames() {

        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(DIRECT_LOOKUP_FILENAME);
            return loadDirectLookupNamesFromInputStream(inputStream);
        } catch (Throwable ex) {
            throw new RuntimeException("failed to load file: '" + DIRECT_LOOKUP_FILENAME+"'",ex);
        }
    }

    private Map<String, String> loadDirectLookupNamesFromInputStream(InputStream inputStream) {

        try {
            Map<String,String> result = new HashMap<String, String>();
            BufferedReader buff = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while (buff.ready()) {
                String line = buff.readLine();
                String[] splitValues = StringUtil.quickSplit(line," ");
                result.put(splitValues[0].toLowerCase(),splitValues[1].toLowerCase());
            }
            buff.close();
            return result;
            
        } catch (Throwable ex) {
            throw new RuntimeException("failed to load direct lookup names",ex);
        }
    }

    private String normalizeRoomaji(String romaaji) {
        
        romaaji = StringUtil.quickReplace(romaaji, "ei", "ee");
        romaaji = StringUtil.quickReplace(romaaji, "ou", "oo");
        romaaji = StringUtil.quickReplace(romaaji, "tch", "cch");
        romaaji = StringUtil.quickReplace(romaaji, "si", "shi");
        romaaji = StringUtil.quickReplace(romaaji, "zi", "ji");
        romaaji = StringUtil.quickReplace(romaaji, "mp", "np");
        romaaji = StringUtil.quickReplace(romaaji, "mb", "nb");
        return romaaji;
    }

}
