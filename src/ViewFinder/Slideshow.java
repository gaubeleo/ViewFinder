package ViewFinder;

import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class Slideshow extends BorderPane {
    private final ImageHandler imageHandler;

    private BorderPane layout;

    private ImageViewPane imageContainer;
    private SettingsPanel settings;
    private InfoPanel info;


    private int index;

    Slideshow(ImageHandler imageHandler){
        this.imageHandler = imageHandler;

        this.index = 0;
    }

    public void create(){
        layout = new BorderPane();

        ImageView slideshowImage = new ImageView();
        slideshowImage.setPreserveRatio(true);
        slideshowImage.setSmooth(true);
        slideshowImage.setImage(imageHandler.get(index));
        slideshowImage.setCache(true);
        slideshowImage.setCacheHint(CacheHint.SCALE);

        imageContainer = new ImageViewPane(slideshowImage);
        imageContainer.setImageView(slideshowImage);

        settings = SettingsPanel.singleton();

        layout.setCenter(imageContainer);
        layout.setLeft(settings);
        layout.setRight(info);

        layout.setMargin(imageContainer, new Insets(50, 50, 50, 50));

        backgroundHandler = new BackgroundHandler(layout, globalSettings.backgroundColor);
        frame = backgroundHandler.createFrame(frameWidth);
    }

    private void increase_index() {
        change_index(1);
    }

    private void reduce_index() {
        change_index(-1);
    }

    private void change_index(int i) {
        index = getRealIndex(index + i);
    }

    public int getRealIndex(int i) {
        if (i < 0){
            return imageHandler.getFileCount()+i;
        }
        else if (i >= imageHandler.getFileCount()){
            return i-imageHandler.getFileCount();
        }
        return i;
    }
}
