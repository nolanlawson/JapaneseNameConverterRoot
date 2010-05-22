package com.nolanlawson.japanesenamegenerator.v3.data;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 * @author nolan
 */
public class XMLModelMarshaller {

    public static void writeToXmlFile(Model model, String filename) {
        try {
            PrintStream printStream = new PrintStream(new File(filename));

            XMLEncoder xmlEncoder = new XMLEncoder(printStream);

            xmlEncoder.writeObject(model);

            xmlEncoder.close();
            System.out.println("Successfully wrote to file: '" + filename+"'");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static Model readFromXmlFile(String filename) {
        try {
            InputStream in = new FileInputStream(new File(filename));

            Model model = readFromXmlInputStream(in);

            System.out.println("Successfully read from file: '" + filename+"'");
            return model;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Model readFromXmlSystemResource(String filename) {
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream(filename);

            Model model = readFromXmlInputStream(in);

            System.out.println("Successfully read from system resource: '" + filename+"'");
            
            return model;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Model readFromXmlInputStream(InputStream in) {
        try {

            XMLDecoder xmlDecoder = new XMLDecoder(in);
            Model model = (Model)xmlDecoder.readObject();

            xmlDecoder.close();

            return model;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
