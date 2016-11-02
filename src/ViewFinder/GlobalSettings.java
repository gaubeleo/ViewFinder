package ViewFinder;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.*;

public class GlobalSettings extends ImageSettings{
    private static GlobalSettings instance;

    public boolean fullscreen;

    public File imagePath;
    public int preloadCount;
    public String onStartAction;

    public Color backgroundColor;
    public Color panelColor;

    public Duration fadeDuration;
    private GlobalSettings(String projectName) {
        super("GlobalSettings", projectName);

        fullscreen = false;

        imagePath = new File("H:\\Images\\Scotland - Isle ofSkye\\fancy");
        preloadCount = 2;
        onStartAction = "Default";

        //set ImageSettings
        backgroundColor = Color.gray(0.25);
        frameColor = Color.gray(1.);
        frameSize = 3;
        hasFrame = true;

        panelColor = Color.gray(0.35);
        fadeDuration = new Duration(350);
    }

    public static GlobalSettings singleton(){
        if (instance == null)
            instance = new GlobalSettings("Default");

        return instance;
    }

    @Override
    public boolean save() {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);

            writeSettings(fileWriter);
            writeImageSettings(fileWriter);

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void writeSettings(PrintWriter fileWriter){
        //handle "null"-Exceptions for primitive types

        if (imagePath != null)
            fileWriter.println("imagePath -> "+imagePath.getAbsolutePath());
        if (fadeDuration != null)
            fileWriter.println("fadeDuration -> "+String.valueOf((int)fadeDuration.toMillis()));
        fileWriter.println("preloadCount -> "+String.valueOf(preloadCount));
        if (panelColor != null)
            fileWriter.println("panelColor -> "+panelColor.toString()); // format: 0xffffffff
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

                if (!handleId(id, value))
                    handleImageId(id, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean handleId(String id, String value){
        try{
            switch(id){
                case "imagePath":
                    imagePath = new File(value);
                    break;
                case "fadeDuration":
                    fadeDuration = new Duration(Integer.valueOf(value));
                    break;
                case "preloadCount":
                    preloadCount = Integer.valueOf(value);
                    break;
                case "panelColor":
                    panelColor = Color.valueOf(value);
                    break;
                case "onStartAction":
                    onStartAction = value;
                    break;
                default:
                    //System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
                    return false;
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        return true;
    }


    public void newProject(String projectName, File imagePath){
        this.imagePath = imagePath;

        changeFilePath(fileName, projectName);
        save();
    }

    public boolean openProject(String openProjectName){
        changeFilePath("GlobalSettings.set", openProjectName);
        if (!file.exists()){
            System.out.println(file.getAbsolutePath());
            return false;
        }
        return load();
    }
}
