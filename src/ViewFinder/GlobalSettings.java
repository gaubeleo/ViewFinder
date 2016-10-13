package ViewFinder;

import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GlobalSettings extends Settings{
    private static GlobalSettings instance;

    public String path;
    public boolean fullscreen;
    public int preloadCount;

    public Color backgroundColor;
    public Color panelColor;
    public Color frameColor;

    public int frameSize;

    public Duration fadeDuration;

    private GlobalSettings(String projectName) {
        super("GlobalSettings", projectName);

        setPath("H:\\Images\\Scotland - Isle of Skye\\fancy");
        setFullscreen(false);
        setPreloadCount(2);

        setBC(Color.gray(0.25));
        setPC(Color.gray(0.35));
        setFC(Color.gray(1.));
        setFS(3);
        setFadeDuration(350);
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

    public void setBC(Color backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public void setFadeDuration(int millis) {
        this.fadeDuration = new Duration(millis);
    }

    public void setPC(Color panelColor) {
        this.panelColor = panelColor;
    }

    public void setFC(Color frameColor) {
        this.frameColor = frameColor;
    }

    public void setFS(int frameSize) {
        this.frameSize = frameSize;
    }

    public String getOnStartAction(){
        return "Default";
    }

    public String getDefaultProject(){return path;}

    public Duration getFadeDuration() {
        return fadeDuration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFullscreen(boolean value) {
        this.fullscreen = value;
    }

    public void setPreloadCount(int preloadCount) {
        this.preloadCount = preloadCount;
    }
}
