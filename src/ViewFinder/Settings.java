package ViewFinder;

import java.io.*;

public abstract class Settings {
    protected File file;

    protected String fileName;
    protected String projectName;
    protected final String extension = ".set";

    public Settings(String fileName, String projectName){
        changeFilePath(fileName, projectName);
    }

    protected void changeFilePath(String fileName, String projectName){
        this.fileName = fileName.replace(extension, "")+extension;
        this.projectName = projectName;

        file = new File(".\\data\\projects\\"+this.projectName+"\\"+this.fileName);
    }

    public abstract boolean load();

    public abstract boolean save();
}
