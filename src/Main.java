
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DTS
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final String FILE_NAME = "config.properties";

    private static File config;
    private static Properties properties;

    public static void main(String[] args) {

        // Initialize config and properties object
        try {
            loadOnStartup();
        } catch (FileNotFoundException ex) { // If config.properties file not found
            System.err.println("Error: " + ex.getMessage());
            // Let user enter data for config file and create it
            config = createConfigFile();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        if (!checkConfigFile()) { // If data invalid or error, system shut down
            System.out.println("Data in " + FILE_NAME + " invalid or some errors occured");
            return;
        } else { // Else we proceed to copy files
            System.out.println("Copy is running...");
            List<String> names = copyFile();

            System.out.println("----------- File name -----------");
            for (String fileName : names) {
                System.out.println(fileName);
            }
            System.out.println("Copy is finished");
        }

//        config.deleteOnExit();
    }

    /**
     * Initialize config and properties object from config.properties file. If
     * FileNotFoundException get thrown, properties object will not be
     * initialized any data and may get NullPointerException when access to
     *
     *
     * @throws java.io.FileNotFoundException If config.properties file not found
     */
    public static void loadOnStartup() throws FileNotFoundException, IOException {
        config = new File(FILE_NAME);

        try (InputStream fileReader = new FileInputStream(config)) {
            properties = new Properties();
            // Load data from config.properties file
            properties.load(fileReader);
        }
    }

    /**
     * Check for valid data in config file. All data must be non-empty, and SRC
     * Folder must be a directory and exist on the system
     *
     * @return true if data in config file is valid, false if data is invalid or
     * error occurred
     */
    public static boolean checkConfigFile() {
        boolean isValid = true;

        String CP_FOLDER = properties.getProperty("COPY_FOLDER");
        String DATA_TYPE = properties.getProperty("DATA_TYPE");
        String PATH = properties.getProperty("PATH");

        File cp_folder = new File(CP_FOLDER);

        // Check for empty data
        if (CP_FOLDER.isEmpty()) {
            System.err.println("Source folder is empty");
            isValid = false;
        }

        if (DATA_TYPE.isEmpty()) {
            System.err.println("Data type is empty");
            isValid = false;
        }

        if (PATH.isEmpty()) {
            System.err.println("Path is empty");
            isValid = false;
        }

        // Check whether CP_FOLDER is a directory and exists or not
        if (!cp_folder.exists() || !cp_folder.isDirectory()) {
            System.err.println("COPY_FOLDER is not a directory or the COPY_FOLDER path does not exist");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Copy file from COPY_FOLDER to PATH
     *
     * @return List<String> contains the list of file names copied
     */
    public static List<String> copyFile() {

        List<String> names = new ArrayList<>();

        String src = properties.getProperty("COPY_FOLDER");
        String des = properties.getProperty("PATH");

        File srcFolder = new File(src);
        File desFolder = new File(des);

        if (!desFolder.exists()) {// If PATH folder does not exist, create it
            desFolder.mkdir();
        }

        String[] extensions = properties.getProperty("DATA_TYPE").split("\\s+");

        // File extention filter
        FilenameFilter filter = (File dir, String name) -> {
            for (int i = 0; i < extensions.length; i++) {
                if (name.endsWith(extensions[i])) {
                    return true;
                }
            }
            return false;
        };

        File[] fileLists = srcFolder.listFiles(filter);

        // Copy files to PATH folder
        for (int i = 0; i < fileLists.length; i++) {
            if (fileLists[i].isFile()) {
                try {
                    names.add(fileLists[i].getName());
                    Files.copy(fileLists[i].toPath(), desFolder.toPath().resolve(fileLists[i].getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    System.err.println("Error: " + ex.getMessage() + " " + ex.getCause());
                }
            }
        }

        return names;
    }

    /**
     * Let user input data of config file and return a new config file with data
     * entered
     *
     * @return Return a config file. Return null if can not write data to file
     */
    public static File createConfigFile() {
        properties = new Properties();

        System.out.println("--------- Input config file ---------");

        System.out.print("Enter source folder: ");
        String src = sc.nextLine().trim();
        System.out.print("Enter data type: ");
        String dataType = sc.nextLine().trim();
        System.out.print("Enter destination folder: ");
        String des = sc.nextLine().trim();

        // Set properties
        properties.setProperty("COPY_FOLDER", src);
        properties.setProperty("DATA_TYPE", dataType);
        properties.setProperty("PATH", des);

        // Create output stream for writing properties to config.properties file
        try (FileOutputStream out = new FileOutputStream(config)) {
            // Save config to file
            properties.store(out, "GIDEON VICTORY");
            return config;
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }
}
