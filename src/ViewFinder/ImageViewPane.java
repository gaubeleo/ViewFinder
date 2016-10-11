package ViewFinder;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * Created by Leo on 01.10.2016.
 */
public class ImageViewPane extends Region {
    private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();
    private Rectangle frame;

    public void setFrame(Rectangle frame){
        this.frame = frame;
        getChildren().add(frame);
        frame.toBack();
    }

    public void removeFrame(){
        getChildren().remove(frame);
    }

    public boolean isFramed(){
        return getChildren().contains(frame);
    }

    public void setImageView(ImageView imageView) {
        this.imageViewProperty.set(imageView);
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

            if (isFramed()){
                double frameSize = frame.getStrokeWidth();
                // -2: weird bug fix on edge between image and frame
                frame.setWidth(imageView.getBoundsInParent().getWidth()+frameSize);
                frame.setHeight(imageView.getBoundsInParent().getHeight()+frameSize);
                layoutInArea(frame, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
            }
        }
        super.layoutChildren();
    }

    public ImageViewPane(ImageView imageView) {
        imageViewProperty.addListener((arg0, oldIV, newIV) -> {
            if (oldIV != null) {
                getChildren().remove(oldIV);
            }
            if (newIV != null) {
                getChildren().add(newIV);
            }
        });
        this.imageViewProperty.set(imageView);
    }

    public static int even(double x){
        return ((int)(x/2))*2;
    }
}
