package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Thumbnail extends StackPane {
    protected final GlobalSettings globalSettings;
    private ImageView iv;
    protected Rectangle frame;

    private FadeTransition fadeIn;
    private ScaleTransition scaleIn;

    protected boolean selected;
    protected final int frameSize;
    protected double ratio;
    protected int index;

    public Thumbnail(int index){
        this.index = index;

        globalSettings = GlobalSettings.singleton();
        frameSize = 4;
        selected = false;
        ratio = 0.;

        iv = new ImageView();

        //iv.setPreserveRatio(true);
        //iv.setSmooth(true);
        iv.setCache(true);
        iv.setCacheHint(CacheHint.SCALE);

        getChildren().add(iv);

        setAlignment(Pos.CENTER);

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
        iv.setImage(img);
        ratio = img.getWidth()/img.getHeight();
        createFrame();
    }

    public void createFrame(){
        this.frame = new Rectangle();

        frame.setFill(Color.TRANSPARENT);
        frame.setStrokeWidth(frameSize);

        frame.setStroke(Color.valueOf("#0099e5"));

        resizeFrame();

        frame.toBack();
        deselect();

        getChildren().add(frame);
    }

    public boolean select(){
        if (frame == null)
            return false;
        selected = true;
        frame.setStroke(Color.valueOf("#0099e5"));
        //frame.setVisible(true);
        return true;
    }

    public boolean deselect(){
        if (frame == null)
            return false;
        selected = false;
        frame.setStroke(Color.WHITE);
        //frame.setVisible(false);
        return true;
    }

    public void resizeFrame(){
        Bounds imgBounds = iv.getBoundsInParent();
        double width = imgBounds.getWidth()+frameSize;
        double height = imgBounds.getHeight()+frameSize;
        frame.setWidth(width);
        frame.setHeight(height);
    }

    public void show() {
        fadeIn.play();
        scaleIn.play();
    }

    public double getRatio(){
        return ratio;
    }

    public void setFitSize(int newHeight, int newWidth) {
        iv.setFitHeight(newHeight);
        iv.setFitWidth(newWidth);
        resizeFrame();
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getIndex() {
        return index;
    }
}

class LoadIndicator extends Thumbnail{
    ProgressIndicator pi;

    private int curHeight;
    private int curWidth;

    public LoadIndicator(int index) {
        super(index);

        pi = new ProgressIndicator();
        pi.setMaxHeight(100);
        getChildren().add(pi);

        getChildren().add(new Text(Integer.toString(index)));

        ratio = 1.5;
        curHeight = 150;
        curWidth = 300;
        createFrame();
    }

    @Override
    public void setFitSize(int newHeight, int newWidth) {
        //pi.setMaxHeight(newHeight);
        curHeight = newHeight;
        curWidth = newWidth;
        resizeFrame();
    }

    @Override
    public void resizeFrame(){
        double width = curWidth+frameSize;
        double height = curHeight+frameSize;
        frame.setWidth(width);
        frame.setHeight(height);
    }
}
