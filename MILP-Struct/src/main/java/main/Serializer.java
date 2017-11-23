package main.java.main;

import main.java.graph.GraphType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);

    public static String getDefaultGraphPath(String fileName, GraphType graphType) {
        return Configuration.GRAPH_REPRESENTATIONS_FOLDER + fileName + "_" + graphType
                + (Configuration.OBJ_FUNCTION ? "_obj" : "" ) + ".ser";
    }

    public static String getDefaultStatisticsPath(String fileName, GraphType graphType) {
        return Configuration.GRAPH_REPRESENTATIONS_FOLDER + fileName + "_" + graphType
                + (Configuration.OBJ_FUNCTION ? "_obj" : "" ) + "_statistics.ser";
    }

    public static void serializeToFile(Object object, String filePath) {
        try {
            createDirectoriesToFilePath(filePath);
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            LOGGER.error(i.toString());
        }
    }

    private static void createDirectoriesToFilePath(String filePath) {
        int lastFolderIndex = Math.max(filePath.lastIndexOf("/"), filePath.lastIndexOf("\\"));
        File directories = new File(filePath.substring(0, lastFolderIndex));
        directories.mkdirs();
    }

    public static Object deserializeFromFile(String filePath) {
        Object object = null;

        if (Files.notExists(Paths.get(filePath))) {
            LOGGER.debug("No serialization exists for {}", filePath);
            return null;
        }

        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            object =  in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            LOGGER.error("IOException when deserializing {}", filePath, i.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return object;
    }
}
