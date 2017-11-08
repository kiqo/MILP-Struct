package main.java.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);

    public static void serializeToFile(Object object, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(Configuration.GRAPH_REPRESENTATIONS_FOLDER  + fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
            LOGGER.debug("Serialized to " + Configuration.GRAPH_REPRESENTATIONS_FOLDER + fileName);
        } catch (IOException i) {
            LOGGER.error(i.toString());
        }
    }

    public static Object deserializeFromFile(String fileName) {
        Object object = null;
        try {
            FileInputStream fileIn = new FileInputStream(Configuration.GRAPH_REPRESENTATIONS_FOLDER  + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            object =  in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            LOGGER.debug("No serialization exists for " + fileName);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return object;
    }
}
