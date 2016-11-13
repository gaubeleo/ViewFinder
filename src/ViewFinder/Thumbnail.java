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
    private double ratio;

    public Thumbnail(ExpandedFlowPane flowLayout){
        globalSettings = GlobalSettings.singleton();
        parent = flowLayout;

        ratio = 0.;

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
            //parent.fitToWidth();
        });
    }
    public void setImg(Image img){
        setImage(img);
        ratio = img.getWidth()/img.getHeight();
    }

    public void show() {
        fadeIn.play();
        scaleIn.play();
    }

    public double getRatio(){
        return ratio;
    }
}
