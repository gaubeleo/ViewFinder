package ViewFinder;

import javafx.scene.paint.Color;

import java.io.*;

public class Settings {
    private File file;
    private String projectName;
    private String fileName = "GlobalSettings";
    public Color backgroundColor;

    public Settings(String projectName){
        this.projectName = projectName;
    }

    public Settings(String projectName, Color backgroundColor){
        this(projectName);
        setBC(backgroundColor);
    }

    public void setBC(Color backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public void setBC(String hashValue){
        this.backgroundColor = Color.valueOf(hashValue);
    }

    public boolean load(){
        if (file == null)
            getFile();

        if (!file.isFile())
            return false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line, id, value;
            String[] splitLine;
            while ((line = reader.readLine()) != null && line != "\n") {
                assert(line.contains(" -> "));
                splitLine = line.split(" -> ", 2);
                id = splitLine[0].trim();
                value = splitLine[1].trim();

                switch(id){
                    case "bgc":
                        setBC(value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void save(){
        if (file == null)
            getFile();
        File dir = file.getParentFile();
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);
            fileWriter.println("bgc -> "+backgroundColor.toString());

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File getFile(){
        file = new File(".\\data\\settings\\"+projectName+"\\"+fileName);
        return file;
    }
}
