package com.nolanlawson.japanesenamegenerator.v3.training;

import com.nolanlawson.japanesenamegenerator.v3.data.Model;
import com.nolanlawson.japanesenamegenerator.v3.data.ModelMarshaller;
import com.nolanlawson.japanesenamegenerator.v3.data.Rule;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.util.Util;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nolan
 */
public class TrainerRunner {

    private static final String MODEL_FILENAME = "/tmp/roomaji_model_20090129_pop1_3_3_min2_even_fewer_rules.txt";

    public static void main(String[] args) {
        train();
        test();
    }

    public static void train() {

        Trainer trainer = new Trainer();

        //List<Pair<String,String>> trainingData = getLargeTrainingData("all_names.txt",100000);
        List<Pair<String,String>> trainingData = getLargeTrainingData("popular_names.txt",10000000);
        //List<Pair<String,String>> trainingData = getSmallTrainingData();


        long time = System.currentTimeMillis();
        Model model = trainer.trainModel(trainingData, 10000, 2);
        System.out.println("Training took " + (System.currentTimeMillis() - time) + " ms total");

        System.out.println("Total # rules: " + model.getRules().size());


        for (Rule rule : model.getRules()) {
            System.out.println(rule);
        }

        int correct = 0;
        int incorrect = 0;
        double averageEditDistance = 0.0;


        for (Pair<String,String> data: trainingData) {
            String transformed = model.transformString(data.getFirst());
            //System.out.println(data + " --> " + transformed);
            if (transformed.equals(data.getSecond())) {
                correct++;
            } else {
                //System.out.println("   ^ Error!");
                incorrect++;
            }
            averageEditDistance += Util.computeLevenshteinDistance(transformed, data.getSecond());
        }
        averageEditDistance /= trainingData.size();

        System.out.println("correct: " + correct);
        System.out.println("incorrect: " + incorrect);
        System.out.println("accuracy: " + (correct * 1.0)/(correct + incorrect));
        System.out.println("average edit distance: " + averageEditDistance);

        ModelMarshaller.writeToFile(model, MODEL_FILENAME);
        

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

    private static List<Pair<String,String>> getSmallTrainingData() {
        List<Pair<String,String>> trainingData = new ArrayList<Pair<String, String>>();

        trainingData.add(new Pair<String,String>("nolan","nooran"));
        trainingData.add(new Pair<String,String>("logan","roogan"));
        trainingData.add(new Pair<String,String>("mel","meru"));
        trainingData.add(new Pair<String,String>("roland","roorando"));
        trainingData.add(new Pair<String,String>("michael","maikeru"));
        trainingData.add(new Pair<String,String>("adam","adamu"));
        trainingData.add(new Pair<String,String>("thea","teea"));
        trainingData.add(new Pair<String,String>("gabe","geebu"));
        trainingData.add(new Pair<String,String>("vincent","binsento"));
        trainingData.add(new Pair<String,String>("valery","barerii"));
        trainingData.add(new Pair<String,String>("alice","arisu"));
        trainingData.add(new Pair<String,String>("john","jon"));
        trainingData.add(new Pair<String,String>("jim","jimu"));
        trainingData.add(new Pair<String,String>("bruce","burusu"));
        trainingData.add(new Pair<String,String>("luigi","ruiji"));
        trainingData.add(new Pair<String,String>("maevis","meebisu"));
        trainingData.add(new Pair<String,String>("ramona","ramona"));
        trainingData.add(new Pair<String,String>("steve","sutiibu"));

        return trainingData;
    }

    private static void test() {

        Model model = ModelMarshaller.readFromFile(MODEL_FILENAME);

        int correct = 0;
        int incorrect = 0;
        double averageEditDistance = 0.0;

        System.out.println();
        for (Pair<String,String> pair : getSmallTrainingData()) {
            String transformedString = model.transformString(pair.getFirst());
            System.out.println(pair + " --> " + transformedString);
            if (transformedString.equals(pair.getSecond())) {
                correct++;
            } else {
                incorrect++;
            }
            averageEditDistance += Util.computeLevenshteinDistance(transformedString, pair.getSecond());

        }
        averageEditDistance /= getSmallTrainingData().size();

        System.out.println();

        System.out.println("correct: " + correct);
        System.out.println("incorrect: " + incorrect);
        System.out.println("accuracy: " + (correct * 1.0)/(correct + incorrect));
        System.out.println("average edit distance: " + averageEditDistance);
    }

}
