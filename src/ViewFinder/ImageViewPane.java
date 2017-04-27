package ViewFinder;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;


public class ImageViewPane extends Region {
    private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();
    private Rectangle frame;

    public ImageViewPane() {
        imageViewProperty.addListener((arg0, oldIV, newIV) -> {
            if (oldIV != null) {
                getChildren().remove(oldIV);
            }
            if (newIV != null) {
                getChildren().add(newIV);
            }
        });
    }

    public ImageViewPane(ImageView imageView){
        this();
        setImageView(imageView);
    }

    public void setFrame(Rectangle frame){
        this.frame = frame;
        getChildren().add(frame);
        frame.toBack();
    }

    public void toggleFrame(){
        if (frame.isVisible())
            frame.setVisible(false);
        else
            frame.setVisible(true);
    }

    public void setImageView(ImageView imageView) {
        imageViewProperty.set(imageView);
    }

    @Override
    protected void layoutChildren() {
        ImageView imageView = imageViewProperty.get();

        int width = even(getWidth());
        int height = even(getHeight());

        if (imageView != null) {
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            layoutInArea(imageView, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);

            if (frame != null && frame.isVisible()){
                double frameSize = frame.getStrokeWidth();
                frame.setWidth(imageView.getBoundsInParent().getWidth()+frameSize);
                frame.setHeight(imageView.getBoundsInParent().getHeight()+frameSize);
                layoutInArea(frame, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
            }
        }
        super.layoutChildren();
    }

    public static int even(double x){
        return ((int)(x/2))*2;
    }
}
