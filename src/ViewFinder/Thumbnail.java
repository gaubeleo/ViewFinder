package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Thumbnail extends ImageView {
    private final GlobalSettings globalSettings;
    private final ExpandedFlowPane parent;

    private FadeTransition fadeIn;
    private ScaleTransition scaleIn;

    public Thumbnail(ExpandedFlowPane flowLayout){
        globalSettings = GlobalSettings.singleton();
        parent = flowLayout;

        setPreserveRatio(true);
        setSmooth(true);

        createAnimations();
    }

    public void createAnimations(){

        fadeIn = new FadeTransition(globalSettings.fadeDuration, this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        scaleIn = new ScaleTransition(globalSettings.fadeDuration, this);
        scaleIn.setFromX(0.7);
        scaleIn.setToX(1.0);
        scaleIn.setFromY(0.7);
        scaleIn.setToY(1.0);

        scaleIn.setOnFinished(e->{
            parent.expandToWidth();
        });
    }

    public void show(Image thumbnail) {
        Platform.runLater(()->{
            setImage(thumbnail);
            parent.expandToWidth();
            fadeIn.play();
            //scaleIn.play();
        });
    }
}
