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
     * @param path
     */
    public ClassCollector(String path) {        //if path is declared, init stream, list, file
        folder = new File(path);
        list = new ArrayList<>();
        classesList = new ArrayList<>();
    }

    /**
     * @param folder
     */
    private void getAllFiles(File folder) {       //return an arrayList with all files which the given folder or file contains
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
     *
     */
    public void generateFileList() {
        list.clear();
        getAllFiles(this.folder);
    }


    public void printFile() {  //generates in the folder from the given path an file as Test.txt und write all absolute pathes from all files in it
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

