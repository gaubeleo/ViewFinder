package ViewFinder;

import java.io.*;

public abstract class Settings {
    protected File file;

    protected String fileName;
    protected String projectName;

    public Settings(String fileName, String projectName){
        this.fileName = fileName;
        this.projectName = projectName;

        file = new File(".\\data\\settings\\"+projectName+"\\"+fileName);
    }

    public abstract boolean load();

    public abstract boolean save();
}
