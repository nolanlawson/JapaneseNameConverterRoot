/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nolanlawson.japanesenamegenerator.v3;

import com.nolanlawson.japanesenamegenerator.v3.katakana.KatakanaConverter;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nolan
 */
public class TestKatakanaConversion {

    public static void main(String[] args) throws Exception {
        KatakanaConverter kc = new KatakanaConverter();
        for (Pair<String,String> pair : getLargeTrainingData("all_names.txt", Integer.MAX_VALUE)) {
            System.out.print(pair.getSecond());
            System.out.print(" " + kc.convertToKatakana(pair.getSecond()));
            System.out.println("\n");
        }
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
