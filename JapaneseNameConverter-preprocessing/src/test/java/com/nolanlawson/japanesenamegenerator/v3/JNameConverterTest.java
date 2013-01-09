package com.nolanlawson.japanesenamegenerator.v3;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiGenerator;
import com.nolanlawson.japanesenamegenerator.v3.kanji.KanjiResult;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;

/**
 * These unit tests should serve as ersatz documentation, in that you can see how to use the basic classes this way.
 * 
 * @author nolan
 *
 */
public class JNameConverterTest {

    @Test
    public void testConvertToRomaajiAndKatakana() throws ConversionException {
        
        // contains a basic English -> roomaji model for unit testing
        InputStream romaajiModelInputStream = ClassLoader.getSystemResourceAsStream(
                "roomaji_model_unit_testing.txt");
        
        // contains a list of all English names with their Japanese equivalents
        InputStream directLookupInputStream = ClassLoader.getSystemResourceAsStream(
                "all_names.txt");
        
        JapaneseNameGenerator generator = new JapaneseNameGenerator(
                romaajiModelInputStream, directLookupInputStream);
        
        // these are pretty inaccurate due to the junit-test-only model
        Assert.assertEquals(Pair.create("arekkusu","\u30a2\u30ec\u30c3\u30af\u30b9"), 
                generator.convertToRomaajiAndKatakana("alex"));
        Assert.assertEquals(Pair.create("nooran", "\u30ce\u30fc\u30e9\u30f3"), 
                generator.convertToRomaajiAndKatakana("nolan"));
        Assert.assertEquals(Pair.create("bibian", "\u30d3\u30d3\u30a2\u30f3"), 
                generator.convertToRomaajiAndKatakana("vivian"));
        Assert.assertEquals(Pair.create("karen", "\u30ab\u30ec\u30f3"), 
                generator.convertToRomaajiAndKatakana("karen"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testConvertToKanji() {
        
        InputStream kanjiInputStream = ClassLoader.getSystemResourceAsStream("kanji_dictionary_unit_testing.csv");
        
        KanjiGenerator kanjiGenerator = new KanjiGenerator(kanjiInputStream);
        
        List<List<KanjiResult>> kanjiResultLists = kanjiGenerator.generateKanji("fuka");
        
        Assert.assertEquals(Arrays.asList(
                Collections.singletonList(new KanjiResult("\u4e0d", "fu", "not")), 
                Collections.singletonList(new KanjiResult("\u4e0b", "ka", "below"))), kanjiResultLists);
        
    }
}
