package ViewFinder;

import javafx.scene.paint.Color;
import java.io.*;

public class ImageSettings extends Settings {
    public Color backgroundColor;
    public boolean hasFrame;
    public Color frameColor = new Color(1., 1., 1., 1.);
    public int frameSize = 5;


    public ImageSettings(String fileName, String projectName, Color backgroundColor){
        super(fileName, projectName);
        setBC(backgroundColor);
        this.hasFrame = false;
        this.fileName = fileName;
    }

    public ImageSettings(String fileName, String projectName, Color backgroundColor, boolean hasFrame){
        super(fileName, projectName);
        setBC(backgroundColor);
        this.hasFrame = true;
        this.fileName = fileName;
    }

    public ImageSettings(String fileName, String projectName, Color backgroundColor, Color frameColor, int frameSize){
        super(fileName, projectName);
        setBC(backgroundColor);
        this.hasFrame = true;
        this.frameColor = frameColor;
        this.frameSize = frameSize;
        this.fileName = fileName;
    }

    @Override
    public boolean save(){
        File dir = file.getParentFile();
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);
            fileWriter.println("method needs to be overwritten in Subclass");

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean load(){
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

    public void setBC(Color backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public void setBC(String hashValue){
        this.backgroundColor = Color.valueOf(hashValue);
    }
}