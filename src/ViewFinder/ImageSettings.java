package ViewFinder;

import javafx.scene.paint.Color;
import java.io.*;

public class ImageSettings extends Settings {
    public Color backgroundColor;
    public boolean hasFrame;
    public Color frameColor;
    public int frameSize;



    public ImageSettings(String fileName, String projectName){
        super(fileName, projectName);
        this.hasFrame = false;

        backgroundColor = Color.gray(0.25);
        frameColor = Color.gray(1.);
        frameSize = 3;
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

                handleImageId(id, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void handleImageId(String id, String value){
        switch(id){
            case "backgroundColor":
                backgroundColor = Color.valueOf(value);
                break;
            case "frameColor":
                frameColor = Color.valueOf(value);
                break;
            case "frameSize":
                frameSize = Integer.valueOf(value);
                break;
            default:
                System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
        }
    }

}