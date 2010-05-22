package com.nolanlawson.japanesenamegenerator.v3;

import com.nolanlawson.japanesenamegenerator.v3.data.Model;
import com.nolanlawson.japanesenamegenerator.v3.data.ModelMarshaller;
import com.nolanlawson.japanesenamegenerator.v3.data.XMLModelMarshaller;
import com.nolanlawson.japanesenamegenerator.v3.katakana.KatakanaConverter;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nolan
 */
public class Test {

        static Map<String,String> testData = new HashMap<String, String>() {{

            put("eustice", "yuusutisu");
            put("thompson", "tompuson");
            put("manning", "maningu");
            put("west", "wesuto");
            put("thea", "sia");
            put("maevis", "meebisu");
            put("stacia", "suteishia");
            put("hartleben", "haatoreben");
            put("lawson", "rooson");
            put("larson", "raason");
            put("smith", "sumisu");
            put("pound", "pondo");
            put("damon", "deemon");
            put("poolman", "puuruman");
            put("beal", "biiru");
            put("king", "kingu");
            put("potter", "pottaa");
            put("bellow", "beroo");
            put("melville", "merubiru");
            put("parker", "paakaa");
            put("walker", "wookaa");
            put("pope", "poopu");
            put("brubeck", "buruubekku");
            put("vorwaller", "boowaraa");
            put("lovitz", "robitsu");
            put("picard", "pikaado");
            put("lambert", "ranbaato");
            put("berzins", "baazinzu");
            put("hermann", "haaman");
            put("meliha", "meriha");
            put("anderson", "andaason");
            put("boyd", "boido");
            put("bardock", "baadokku");
            put("johnson", "jonson");
            put("clinton", "kurinton");
            put("sanders", "sandaazu");
            put("hanson", "hanson");
            put("bart", "baato");
            put("gerald", "jerarudo");
            put("perkowitz", "paakowitsu");
            put("ferrel", "fereru");
            put("butler", "batoraa");
            put("maynard", "meenaado");
            put("nancy", "nanshii");
            put("durup", "durupu");
            put("holly", "hoorii");
            put("eaton", "iiton");
            put("pittman", "pittoman");

        }};

    public static void main(String[] args) throws Exception {
        
        List<String> filenames = new ArrayList<String>();

        File dirFile = new File("/home/nolan/Desktop/old/models");
        for (File file : dirFile.listFiles()) {
            filenames.add(file.getPath());
        }

        Collections.sort(filenames);

        for (String filename : new String[]{"/home/nolan/Desktop/old/models/roomaji_model_20090128_pop1_3_3_min2_fewer_rules.txt"}) {
            test1(filename);
        }
    }

    private static void testJapaneseNameGenerator() {

        JapaneseNameGenerator japaneseNameGenerator = new JapaneseNameGenerator();

        List<String> names = Arrays.asList("Nolan Lawson","JT", "J.T. Thompson", "DJ Manning", 
                "Kevin Eustice","Meliha Yetisgen Yildiz","Pat Ferrel", "Mike Perkowitz", "Kenji Kawai",
                "Kanye West", "Thea Lawson", "Maevis Lawson", "Stacia Hartleben", "Shannon Welle", "Will Smith",
                "Ezra Pound", "Gabriel Damon", "Adam Poolman", "Justin Beal", "Stephen King", "Harry Potter", "Jesus Christ",
                "Saul Bellow", "D'Angelo Martin", "Gary Davis", "Herman Melville", "Tray Parker", "Matt Brubeck",
                "Marcus Vorwaller", "Jon Lovitz", "Matt Groening", "Mary Walker", "Kristi Pope", "Jean-Luc Picard",
                "Jean-Paul Jones","Hannako Lambert", "Hunter Thompson");

        try {
            for (String name : names) {
                System.out.println(name + " -- > " + japaneseNameGenerator.convertToRomaajiAndKatakana(name));
            }
            
        } catch (ConversionException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }



    }
    
    private static void test1(String filename) {
        Model model = ModelMarshaller.readFromFile(filename);

        int correct = 0;
        int incorrect = 0;
        int totalEditDistance = 0;

        for (Entry<String, String> entry : testData.entrySet()) {
            String output = model.transformString(entry.getKey());
            totalEditDistance += Util.computeLevenshteinDistance(output, entry.getValue());
            
            if (output.equals(entry.getValue())) {
                correct++;
                System.out.println(String.format("%s -> %s", entry.getKey(),output,entry.getValue()));
            } else {
                System.out.println(String.format("%s -> %s (gold: %s)", entry.getKey(),output,entry.getValue()));

                incorrect++;
            }

        }

        System.out.println("\n" + filename.replaceAll("^.*/", ""));
        System.out.println("Accuracy: " + (1.0 * correct/(correct + incorrect)));
        System.out.println("Total edit distance: " + totalEditDistance + "\n");
    }
    
    private static void test2() {
        Model model = XMLModelMarshaller.readFromXmlSystemResource("romaaji_model_20090118_1.ser");
        
        KatakanaConverter katakanaConverter = new KatakanaConverter();
        
        for (String name : getTestData()) {
            
            try {
                String romaaji = model.transformString(name);

                String katakana = katakanaConverter.convertToKatakana(romaaji);
                System.out.println(name +" --> " + katakana);
            } catch (Exception e) {
                System.out.println("couldn't process name: " + name);
                e.printStackTrace();
            }
        }
    }

    private static List<String> getTestData() {

        return Arrays.asList("nolan","shannon","bruce","adam","raymond","antoine","timothy","thea",
                "maevis","bartz","mozart","jason","jared","jill","kanye","yeshiva",
                "stacia","lawson","larson","larsen","anderson","buddy","kevin","meliha","kenji","cory","danielle","daniel");


    }

    private static List<Pair<String,String>> getLargeTrainingData(String dataFilename, int limit) {

        try {
            List<Pair<String,String>> result = new ArrayList<Pair<String, String>>();
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(dataFilename);
            BufferedReader buff = new BufferedReader(new InputStreamReader(inputStream));
            while (buff.ready()) {
                String line = buff.readLine();
                String[] strPair = line.split("\\s+");
                result.add(new Pair<String,String>(strPair[0],strPair[1]));
            }
            //Collections.shuffle(result);
            if (result.size() > limit) {
                result = result.subList(0, limit);
            }
            return result;
        } catch (Throwable t) {
            throw new RuntimeException("failed to read in file", t);
        }

    }

}
