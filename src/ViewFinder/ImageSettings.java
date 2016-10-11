package ViewFinder;

import javafx.scene.paint.Color;

public class ImageSettings extends Settings {
    private String fileName = "_LocalSettings_";

    public boolean hasFrame;
    public Color frameColor = new Color(1., 1., 1., 1.);
    public int frameSize = 5;


    public ImageSettings(String projectName, String fileName, Color backgroundColor){
        super(projectName, backgroundColor);
        this.hasFrame = false;
        this.fileName = fileName;
    }

    public ImageSettings(String projectName, String fileName, Color backgroundColor, boolean hasFrame){
        super(projectName, backgroundColor);
        this.hasFrame = true;
        this.fileName = fileName;
    }

    public ImageSettings(String projectName, String fileName, Color backgroundColor, Color frameColor, int frameSize){
        super(projectName, backgroundColor);
        this.hasFrame = true;
        this.frameColor = frameColor;
        this.frameSize = frameSize;
        this.fileName = fileName;
    }
}