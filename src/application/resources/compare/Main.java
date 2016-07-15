package application.resources.compare;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mark-4304
 */
public class Main {

    private static String path;
    private static String baseApplicationResources;
    private static String baseJSApplicationResources;
    private static List cmpApplicationResourcesList;
    private static List cmpJSApplicationResourcesList;

    /**
     * Take List of ApplicationResources and JSApplicationResources to be
     * compare
     *
     * @param path the command line path
     */
    public static void readPath(String path) throws IOException {
        baseApplicationResources = path.toString() + "\\ApplicationResources.properties";
        baseJSApplicationResources = path.toString() + "\\JSApplicationResources.properties";

        cmpApplicationResourcesList = new ArrayList<String>();
        cmpJSApplicationResourcesList = new ArrayList<String>();

        Files.walk(Paths.get(path)).forEach(filePath -> {

            if (Files.isRegularFile(filePath)) {
                if (filePath.toString().indexOf("\\ApplicationResources_") >= 0) {
                    cmpApplicationResourcesList.add(filePath.toString());
                }
                if (filePath.toString().indexOf("\\JSApplicationResources_") >= 0) {
                    cmpJSApplicationResourcesList.add(filePath.toString());
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {

        readPath(args[0]);

        String filePath = baseApplicationResources;
        Map baseContent = getSearchableContent(filePath, true);
        Iterator<String> iterator = cmpApplicationResourcesList.iterator();
        while (iterator.hasNext()) {
            filePath = iterator.next();
            Map content = getSearchableContent(filePath, false);
            CompareStringBuilder builder = compareContents(baseContent, content);
            createReport(filePath, builder);

        }

        filePath = baseJSApplicationResources;
        baseContent = getSearchableContent(filePath, true);
        iterator = cmpJSApplicationResourcesList.iterator();
        while (iterator.hasNext()) {
            filePath = iterator.next();
            Map content = getSearchableContent(filePath, false);
            CompareStringBuilder builder = compareContents(baseContent, content);
            createReport(filePath, builder);
        }

    }

    /**
     * Take List of ApplicationResources and JSApplicationResources to be
     * compare
     *
     * @param path the command line path
     */
    public static CompareStringBuilder compareContents(Map baseContent, Map content) {
        CompareStringBuilder builder = new CompareStringBuilder();

        for (Iterator it = baseContent.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, LinkedHashSet<String>> packageEntry = (Map.Entry<String, LinkedHashSet<String>>) it.next();
            String packageName = packageEntry.getKey();

            try {
                //Finding Extra Keys
                Set baseKeysSet = (LinkedHashSet<String>) baseContent.get(packageName);
                Set keysSet = (LinkedHashSet<String>) content.get(packageName);
                Iterator<String> iterator = keysSet.iterator();
                while (iterator.hasNext()) {
                    String keyName = iterator.next();
                    if (!baseKeysSet.contains(keyName)) {
                        builder.rightSideStringBuilder.append(packageName + "." + keyName + "\n");
                        //System.out.println(packageName + "." + keyName);
                        builder.extraCount++;
                    }
                }
            } catch (Exception ex) {
                //Missing Package Missing Key 
                //System.err.println("Not Package Avaible in tmp" + baseContent.get(packageName));
                Set KeysSet = (LinkedHashSet<String>) baseContent.get(packageName);
                Iterator<String> iterator = KeysSet.iterator();
                while (iterator.hasNext()) {
                    String keyName = iterator.next();

                    builder.leftSideStringBuilder.append(packageName + "." + keyName + "\n");
                    //System.out.println(packageName + "." + keyName);
                    builder.missingCount++;

                }
            }

            try {//Find Missing Individual Key
                Set keysSet = (LinkedHashSet<String>) content.get(packageName);
                Set baseKeys = (Set<String>) baseContent.get(packageName);
                Iterator<String> iterator = baseKeys.iterator();
                while (iterator.hasNext()) {
                    String keyName = iterator.next();
                    if (!keysSet.contains(keyName)) {
                        builder.leftSideStringBuilder.append(packageName + "." + keyName + "\n");
                        builder.missingCount++;
                    }
                }
            } catch (Exception ex) {
               // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "  --- : "+ packageName, ex);
                Set KeysSet = (LinkedHashSet<String>) baseContent.get(packageName);
                Iterator<String> iterator = KeysSet.iterator();
                while (iterator.hasNext()) {
                    String keyName = iterator.next();

                    builder.leftSideStringBuilder.append(packageName + "." + keyName + "\n");
                    //System.out.println(packageName + "." + keyName);
                    builder.missingCount++;

                }
            }

        }
        return builder;
    }

    /**
     * To Get Searchable Content Type object from given file
     *
     * @param filepath location of the file
     */
    public static Map getSearchableContent(String filePath, Boolean isCheck) {
        File file = new File(filePath);
        Scanner scanner = null;

        Map searchableContent = new LinkedHashMap<String, LinkedHashSet<String>>();
        String previousPackageName = null;
        String packageName = null;
        String keyName = null;
        Set keys = null;

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "File Not Found", ex);
        }

        //scanning line by line 
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            //skip line supports starts with ## or empty line
            if (line.startsWith(" ") || line.startsWith("#") || line.length() < 2) {
                continue;
            }

            //finding package name and key name
            String[] strArray = line.split("=");
            String str = strArray[0];
            int index = str.lastIndexOf(".");
            try {
                packageName = str.substring(0, index);
                keyName = str.substring(index + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                try {
                    int index1 = str.lastIndexOf("_");
                    packageName = str.substring(0, index1);
                    keyName = str.substring(index1 + 1);
                } catch (StringIndexOutOfBoundsException ex1) {
                    // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "String Index Out Of Bounds :  " + line, ex1);
                }
            }

            //Update previousPackageName for First Time 
            if (previousPackageName == null) {
                previousPackageName = packageName;
                keys = new LinkedHashSet<String>();
            }

            try {
                //Updating Searchable Content Map
                if (previousPackageName.equals(packageName)) {
                    keys.add(keyName);
                } else {
                    searchableContent.put(previousPackageName, keys);
                    previousPackageName = packageName;

                    if (searchableContent.containsKey(previousPackageName) == true) {
                        keys = (LinkedHashSet<String>) searchableContent.get(previousPackageName);
                    } else {
                        keys = new LinkedHashSet<String>();

                    }
                    keys.add(keyName);

                }
            } catch (Exception e) {
                // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, line, e);
            }

        }
        //Adding Last item to Map
        searchableContent.put(previousPackageName, keys);
        //System.out.println(searchableContent);
        return searchableContent;
    }

    private static void createReport(String filePath, CompareStringBuilder stringBuilder) throws IOException {

        File homeDirectory = javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory();
        int index = filePath.lastIndexOf("\\");
        String fileName = filePath.substring(index + 1, filePath.length() - 11);

        String outputPath = homeDirectory + "\\KeysDifferences\\";
        makeDirectory(outputPath);
        File createReportFile = null;

        System.out.println("Generating Report : " + fileName);
        try {

            createReportFile = new File(outputPath + fileName + ".txt");

            if (createReportFile.createNewFile()) {
                System.out.println(fileName + " is created!");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, createReportFile.toString(), ex);
        }
        /*
         * To write contents of StringBuffer to a file, use
         * BufferedWriter class.
         */
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(createReportFile));
        bufferedWriter.write("   ---------------------- Missing Keys List --------------------  \n\n");
        bufferedWriter.write(stringBuilder.leftSideStringBuilder.toString());
        bufferedWriter.write("\n\n\n\n   ---------------------- Extra Keys List --------------------  \n\n");
        bufferedWriter.write(stringBuilder.rightSideStringBuilder.toString());
        bufferedWriter.write("\n\n\n\n\n ---------------------- Summary --------------------  \n\n");
        bufferedWriter.write("\n\n Number of Missing Keys  : "+ stringBuilder.missingCount);
        bufferedWriter.write("\n\n Number of Extra Keys    : "+ stringBuilder.extraCount);
        bufferedWriter.flush();
        bufferedWriter.close();

        System.out.println("Missing Keys written to File : " + createReportFile.getPath());

    }

    private static void makeDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        // if the directory does not exist, create it
        if (!directory.exists()) {
            boolean result = false;

            try {
                if (directory.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory Not created");
                }
            } catch (SecurityException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, directory.getAbsolutePath(), ex);
            }

        }
    }

}
