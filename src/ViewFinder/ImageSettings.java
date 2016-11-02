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
    }

    @Override
    public boolean save(){
        File dir = file.getParentFile();
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);

            writeImageSettings(fileWriter);

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void writeImageSettings(PrintWriter fileWriter){
        //handle "null"-Exceptions for primitive types


        if (backgroundColor != null)
            fileWriter.println("backgroundColor -> "+backgroundColor); // format: 0xffffffff
        if (frameColor!= null)
            fileWriter.println("frameColor -> "+frameColor); // format: 0xffffffff
        fileWriter.println("frameSize -> "+String.valueOf(frameSize));
        fileWriter.println("hasFrame -> "+String.valueOf(hasFrame));
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

    protected boolean handleImageId(String id, String value){
        try{
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
                case "hasFrame":
                    hasFrame = Boolean.valueOf(value);
                    break;
                default:
                    System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
                    return false;
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        return true;
    }
}