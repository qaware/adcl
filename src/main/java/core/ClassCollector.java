package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ClassCollector {
    private ArrayList<File> list;
    private File folder;
    private ArrayList<Class<?>> classesList;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassCollector.class);

    /**
     * Initialize the instance with a path set through parameters
     * @param path decide from which folder the files are added to the classesList
     */
    public ClassCollector(String path) {
        folder = new File(path);
        list = new ArrayList<>();
        classesList = new ArrayList<>();
    }

    /**
     * Add all files into the list
     * @param folder is root data for all files to add
     */
    private void getAllFiles(File folder) {
        File[] file = folder.listFiles();

        for (int i = 0; i < file.length; i++) {
            File t = file[i];
            if (t.listFiles() != null) {
                getAllFiles(t);
            } else {
                if (t.getName().endsWith(".class")) {
                    LOGGER.info(t.getAbsolutePath());
                    list.add(t);
                }
            }
        }

    }

    /**
     * Calls function getAllFiles with the given Path from constructor
     */
    public void generateFileList() {
        list.clear();
        getAllFiles(this.folder);
    }

    /**
     * Prints classesList out in folder path as Test.txt
     */
    public void printFile() {
        try (PrintWriter out = new PrintWriter(folder.getAbsolutePath() + "/Test.txt")) {
            for (File file : getList()) {
                out.println(file.getAbsolutePath());
            }
        } catch (IOException ex) {
            LOGGER.info(ex.getMessage());
        }
    }

    public File getFolder() { //return folder based on the path in the form of file
        return folder;
    }

    public List<File> getList() {   //return complete file list as ArrayList<File>
        return list;
    }

    public List<Class<?>> getClassesList() {  //return ArrayList<Class<?>>
        return classesList;
    }
}

