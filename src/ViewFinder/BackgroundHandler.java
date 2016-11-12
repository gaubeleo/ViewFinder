package ViewFinder;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BackgroundHandler {

    private static BackgroundHandler instance;
    private Region root;
    private Rectangle frame;

    private Color currentBC;
    private Color nextBC;

    private Transition fadeBC;

    private BackgroundHandler() {
        this(Color.WHITE);
    }

    private BackgroundHandler(Color currentBC) {
        this.currentBC = currentBC;
    }

    public static BackgroundHandler singleton(Color currentBC){
        if (instance == null)
            instance = new BackgroundHandler(currentBC);

        return instance;
    }

    public void setRoot(Region root){
        this.root = root;
        setCurrentBC(currentBC);
    }

    public Rectangle createFrame(int frameWidth){
        this.frame = new Rectangle();

        frame.setFill(Color.TRANSPARENT);
        frame.setStrokeWidth(frameWidth);

        frame.setStroke(Color.WHITE);
        //adjustFrameColor();

        return frame;
    }

    public Transition fadeBackground(Duration duration){
        final Transition fadeBC = new Transition() {
            {
                setCycleDuration(duration);
            }
            @Override
            protected void interpolate(double fraction) {
                Color tempColor = currentBC.interpolate(nextBC, fraction);
                root.setStyle(String.format("-fx-background-color: #%s;", tempColor.toString().substring(2, 8)));
            }
        };
        fadeBC.setOnFinished(e -> {
            transitionFinished();
        });

        return fadeBC;
    }

    public void setCurrentBC(Color currentBC){
        this.currentBC = currentBC;
        root.setStyle(String.format("-fx-background-color: #%s;", currentBC.toString().substring(2, 8)));
    }

    public void setNextBC(Color nextBC){
        this.nextBC = nextBC;
    }

    public void adjustFrameColor(){
        if (nextBC.getBrightness() > 0.5)
            frame.setStroke(Color.BLACK);
        else
            frame.setStroke(Color.WHITE);
    }

    public void transitionFinished(){
        setCurrentBC(nextBC);
    }
}
