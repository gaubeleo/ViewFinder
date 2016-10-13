package ViewFinder;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;

public class GlobalSettings extends Settings{
    private static GlobalSettings instance;

    public File imagePath;
    public boolean fullscreen;
    public int preloadCount;
    public String onStartAction;

    public Color backgroundColor;
    public Color panelColor;
    public Color frameColor;

    public int frameSize;

    public Duration fadeDuration;
    private GlobalSettings(String projectName) {
        super("GlobalSettings", projectName);

        imagePath = new File("H:\\Images\\Scotland - Isle ofSkye\\fancy");
        fullscreen = false;
        preloadCount = 2;
        onStartAction = "Default";

        backgroundColor = Color.gray(0.25);
        panelColor = Color.gray(0.35);
        frameColor = Color.gray(1.);
        frameSize = 3;
        fadeDuration = new Duration(350);

    }

    public static GlobalSettings singleton(){
        if (instance == null)
            instance = new GlobalSettings("Default");

        return instance;
    }

    @Override
    public boolean load() {
        return false;
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
