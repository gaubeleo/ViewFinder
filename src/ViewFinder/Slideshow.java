package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;

public class Slideshow extends BorderPane {
    private final GlobalSettings globalSettings;
    private final ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;

    private ImageViewPane imageContainer;
    private SettingsPanel settings;
    private InfoPanel info;

    ////////////////////////////////////

    private ImageView image;
    private Rectangle frame;

    ////////////////////////////////////

    private FadeTransition imageFadeIn;
    private FadeTransition imageFade;
    private FadeTransition fadeInFrame;
    private FadeTransition frameFade;

    private Transition backgroundFade;

    ////////////////////////////////////

    private int index;
    private int preloadCount;

    ////////////////////////////////////

    Slideshow(GlobalSettings globalSettings, ImageHandler imageHandler){
        this.index = 0;
        this.preloadCount = 2;

        this.globalSettings = globalSettings;
        this.imageHandler = imageHandler;
    }

    public void create(){
        // Background and Frame Handler
        backgroundHandler = new BackgroundHandler(this, globalSettings.backgroundColor);
        frame = backgroundHandler.createFrame(globalSettings.frameSize);

        // Layout Components
        image = new ImageView();
        image.setImage(imageHandler.get(index));
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setCache(true);
        image.setCacheHint(CacheHint.SCALE);

        imageContainer = new ImageViewPane(image);
        imageContainer.setImageView(image);
        imageContainer.setFrame(frame);

        settings = SettingsPanel.singleton();
        info = InfoPanel.singleton();

        setCenter(imageContainer);
        setLeft(settings);
        setRight(info);

        setMargin(imageContainer, new Insets(50, 50, 50, 50));

        // Fade Animations for image/frame/background
        createAnimations();
    }

    public void createAnimations(){
        Duration duration = globalSettings.getFadeDuration();

        imageFadeIn = new FadeTransition(duration, image);
        imageFadeIn.setFromValue(0.0);
        imageFadeIn.setToValue(1.0);

        imageFade = new FadeTransition(duration, image);
        imageFade.setFromValue(1.0);
        imageFade.setToValue(0.0);
        imageFade.setOnFinished(e -> {
            image.setImage(imageHandler.get(index));
            imageFadeIn.play();
        });

        fadeInFrame = new FadeTransition(duration, frame);
        fadeInFrame.setFromValue(0.0);
        fadeInFrame.setToValue(1.0);

        frameFade = new FadeTransition(duration, frame);
        frameFade.setFromValue(1.0);
        frameFade.setToValue(0.0);
        frameFade.setOnFinished(e -> {
            backgroundHandler.adjustFrameColor();
            fadeInFrame.play();
        });

        backgroundFade = backgroundHandler.fadeBackground(duration.multiply(2));
    }

    public boolean chooseDirectory(String path){
        return chooseDirectory(new File(path));
    }

    public boolean chooseDirectory(File path){
        final File[] fileList = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });

        if (fileList == null){
            System.out.format("'%s' is not a valid path!", path);
            return false;
        }
        else if (fileList.length == 0){
            System.out.format("'%s' contains no .jpg images!", path);
            return false;
        }

        imageHandler.addAll(fileList);
        for (int offset = -preloadCount; offset <= preloadCount; offset++){
            if (offset == 0){
                imageHandler.preload(getRealIndex(index));
                image.setImage(imageHandler.get(index));
            }
            else
                imageHandler.preload_threaded(getRealIndex(index+offset));
        }
        return true;
    }


    public void next(){
        backgroundHandler.setNextBC(imageHandler.getBackgroundColor(getRealIndex(index+1)));

        imageFade.play();
        frameFade.play();
        backgroundFade.play();

        increase_index();

        imageHandler.drop(getRealIndex((index-1)-preloadCount));
        imageHandler.preload_threaded(getRealIndex(index+preloadCount));
    }

    public void previous(){
        backgroundHandler.setNextBC(imageHandler.getBackgroundColor(getRealIndex(index-1)));

        imageFade.play();
        frameFade.play();
        backgroundFade.play();

        reduce_index();

        imageHandler.drop(getRealIndex((index+1)+preloadCount));
        imageHandler.preload_threaded(getRealIndex(index-preloadCount));
    }

    public void slidePanels(){
        settings.slideInOut();
        info.slideInOut();
    }

    public void frameImage(){
        imageContainer.toggleFrame();
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
