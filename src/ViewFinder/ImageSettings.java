package ViewFinder;

import javafx.scene.paint.Color;

public class ImageSettings extends Settings {
    public boolean hasFrame;
    public Color frameColor = new Color(1., 1., 1., 1.);
    public int frameSize = 5;


    public ImageSettings(Color backgroundColor){
        super(backgroundColor);
        this.hasFrame = false;
    }

    public ImageSettings(Color backgroundColor, boolean hasFrame){
        super(backgroundColor);
        this.hasFrame = true;
    }

    public ImageSettings(Color backgroundColor, Color frameColor, int frameSize){
        super(backgroundColor);
        this.hasFrame = true;
        this.frameColor = frameColor;
        this.frameSize = frameSize;
    }
}