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
                result.add(Pair.create(strPair[0],strPair[1]));
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

        trainingData.add(Pair.create("nolan","nooran"));
        trainingData.add(Pair.create("logan","roogan"));
        trainingData.add(Pair.create("mel","meru"));
        trainingData.add(Pair.create("roland","roorando"));
        trainingData.add(Pair.create("michael","maikeru"));
        trainingData.add(Pair.create("adam","adamu"));
        trainingData.add(Pair.create("thea","teea"));
        trainingData.add(Pair.create("gabe","geebu"));
        trainingData.add(Pair.create("vincent","binsento"));
        trainingData.add(Pair.create("valery","barerii"));
        trainingData.add(Pair.create("alice","arisu"));
        trainingData.add(Pair.create("john","jon"));
        trainingData.add(Pair.create("jim","jimu"));
        trainingData.add(Pair.create("bruce","burusu"));
        trainingData.add(Pair.create("luigi","ruiji"));
        trainingData.add(Pair.create("maevis","meebisu"));
        trainingData.add(Pair.create("ramona","ramona"));
        trainingData.add(Pair.create("steve","sutiibu"));

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
