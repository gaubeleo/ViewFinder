package ViewFinder;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

        panelColor = Color.gray(0.35);
        fadeDuration = new Duration(350);
    }

    public static GlobalSettings singleton(){
        if (instance == null)
            instance = new GlobalSettings("Default");

        return instance;
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

                handleId(id, value);
                handleImageId(id, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void handleId(String id, String value){
        switch(id){
            case "imagePath":
                imagePath = new File(value);
                break;
            case "preloadCount":
                preloadCount = Integer.valueOf(value);
                break;
            case "panelColor":
                panelColor = Color.valueOf(value);
                break;
            case "fadeDuration":
                fadeDuration = new Duration(Integer.valueOf(value));
                break;
            case "onStartAction":
                onStartAction = value;
                break;
            default:
                System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
        }
    }

    @Override
    public boolean save() {
        return false;
    }

    public void newProject(String projectName, File imagePath){
        this.projectName = projectName;
        this.imagePath = imagePath;

        save();
    }
}
