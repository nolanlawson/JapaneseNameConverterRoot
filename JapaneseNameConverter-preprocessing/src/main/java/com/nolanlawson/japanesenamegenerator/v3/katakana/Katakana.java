package com.nolanlawson.japanesenamegenerator.v3.katakana;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nolan
 */
public enum Katakana {

    SMALL_A ((char)12449),
    A ((char)12450),
    SMALL_I ((char)12451),
    I ((char)12452),
    SMALL_U ((char)12453),
    U ((char)12454),
    SMALL_E ((char)12455),
    E ((char)12456),
    SMALL_O ((char)12457),
    O ((char)12458),
    KA ((char)12459),
    GA ((char)12460),
    KI ((char)12461),
    GI ((char)12462),
    KU ((char)12463),
    GU ((char)12464),
    KE ((char)12465),
    GE ((char)12466),
    KO ((char)12467),
    GO ((char)12468),
    SA ((char)12469),
    ZA ((char)12470),
    SI ((char)12471),
    SHI ((char)12471), // Hepburn representation
    ZI ((char)12472),
    JI ((char)12472), // Hepburn representation
    SU ((char)12473),
    ZU ((char)12474),
    SE ((char)12475),
    ZE ((char)12476),
    SO ((char)12477),
    ZO ((char)12478),
    TA ((char)12479),
    DA ((char)12480),
    TI ((char)12481),
    CHI ((char)12481), // Hepburn representation
    DI ((char)12482),
    SMALL_TU ((char)12483),
    SMALL_TSU ((char)12483),
    TU ((char)12484),
    TSU ((char)12484), // Hepburn representation
    DU ((char)12485),
    DZU ((char)12485), // Hepburn representation
    TE ((char)12486),
    DE ((char)12487),
    TO ((char)12488),
    DO ((char)12489),
    NA ((char)12490),
    NI ((char)12491),
    NU ((char)12492),
    NE ((char)12493),
    NO ((char)12494),
    HA ((char)12495),
    BA ((char)12496),
    PA ((char)12497),
    HI ((char)12498),
    BI ((char)12499),
    PI ((char)12500),
    HU ((char)12501),
    FU ((char)12501), //Hepburn representation
    BU ((char)12502),
    PU ((char)12503),
    HE ((char)12504),
    BE ((char)12505),
    PE ((char)12506),
    HO ((char)12507),
    BO ((char)12508),
    PO ((char)12509),
    MA ((char)12510),
    MI ((char)12511),
    MU ((char)12512),
    ME ((char)12513),
    MO ((char)12514),
    SMALL_YA ((char)12515),
    YA ((char)12516),
    SMALL_YU ((char)12517),
    YU ((char)12518),
    SMALL_YO ((char)12519),
    YO ((char)12520),
    RA ((char)12521),
    RI ((char)12522),
    RU ((char)12523),
    RE ((char)12524),
    RO ((char)12525),
    SMALL_WA ((char)12526),
    WA ((char)12527),
    WI ((char)12528),
    WE ((char)12529),
    WO ((char)12530),
    N ((char)12531),
    VU ((char)12532),
    SMALL_KA ((char)12533),
    SMALL_KE ((char)12534),
    VA ((char)12535),
    VI ((char)12536),
    VE ((char)12537),
    VO ((char)12538),
    PROLONGED_SOUND_MARK ((char)12540),
    DOT ((char)12539);

    private char ch;
    private String str;

    Katakana(char ch) {
        this.ch = ch;
        this.str = Character.toString(ch);
    }

    public char getChar() {
        return ch;
    }

    public String getString() {
        return this.str;
    }

    private static Map<Character,String> reverseLookupMap = buildReverseLookupMap();

    public static String reverseLookup(char ch) {
        return reverseLookupMap.get(ch);
    }

    private static Map<Character, String> buildReverseLookupMap() {
        Map<Character,String> result = new HashMap<Character, String>();

        for (Katakana katakana : Katakana.values()) {
            String existingString = result.get(katakana.ch);
            String currentString = katakana.name().toLowerCase();

            // prefer hepburn roomaji
            if (existingString == null
                    || currentString.length() > existingString.length()
                    || (currentString.equals("ji") && existingString.equals("zi"))
                    || (currentString.equals("fu") && existingString.equals("hu"))) {
                result.put(katakana.ch, currentString);
            }
        }

        return result;
    }
}
