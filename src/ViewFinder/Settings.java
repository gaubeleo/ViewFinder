package ViewFinder;

import java.io.*;

public abstract class Settings {
    protected File file;

    protected String fileName;
    protected String projectName;
    protected final String extension = ".set";

    public Settings(String fileName, String projectName){
        changeFile(fileName, projectName);
    }

    protected void changeFile(String fileName, String projectName){
        this.fileName = fileName;
        this.projectName = projectName;

        file = new File(".\\data\\settings\\"+projectName+"\\"+fileName+extension);
    }

    public abstract boolean load();

    public abstract boolean save();
}
