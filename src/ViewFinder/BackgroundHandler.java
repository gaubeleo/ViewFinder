package ViewFinder;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class BackgroundHandler extends Transition {

    protected Region region;
    protected Color currentBC;
    protected Color nextBC;

    public BackgroundHandler(Region region) {
        this.region = region;
    }

    public BackgroundHandler(Region region, Color currentBC) {
        this(region);
        setCurrentBC(currentBC);
    }

    @Override
    protected void interpolate(double fraction) {
        Color tempColor = currentBC.interpolate(nextBC, fraction);
        region.setStyle(String.format("-fx-background-color: #%s;", tempColor.toString().substring(2, 8)));
    }

    public void setCurrentBC(Color currentBC){
        this.currentBC = currentBC;
        region.setStyle(String.format("-fx-background-color: #%s;", currentBC.toString().substring(2, 8)));
    }

    public void setNextBC(Color nextBC){
        this.nextBC = nextBC;
    }

    public void transitionFinished(){
        Color temp = Color.gray(currentBC.getRed());

        setCurrentBC(nextBC);
        setNextBC(temp);
    }

    public void setAnimationDuration(Duration duration){
        setCycleDuration(duration);
    }
}
