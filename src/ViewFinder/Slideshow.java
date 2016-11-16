package ViewFinder;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Vector;

import static java.lang.Double.MAX_VALUE;

public class Slideshow extends BorderPane {
    private final ViewFinder vf;
    private final GlobalSettings globalSettings;
    private final ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;
    private Thread backgroundThread;

    ////////////////////////////////////

    private ImageViewPane imageContainer;
    private SettingsPanel settings;
    private InfoPanel info;
    private MenuPanel menuPanel;

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

    Slideshow(ViewFinder vf){
        this.vf = vf;
        this.globalSettings = GlobalSettings.singleton();
        this.index = 0;

        this.preloadCount = globalSettings.preloadCount;
        this.imageHandler = ImageHandler.singleton();
    }

    public void create(){
        // Background and Frame Handler
        backgroundHandler = BackgroundHandler.singleton(globalSettings.backgroundColor);
        frame = backgroundHandler.createFrame(globalSettings.frameSize);

        // Layout Components
        image = new ImageView();
        image.setImage(imageHandler.get(index));
        image.setPreserveRatio(true);
        image.setSmooth(true);
        //image.setCache(true);
        //image.setCacheHint(CacheHint.SPEED);

        imageContainer = new ImageViewPane();
        imageContainer.setImageView(image);
        imageContainer.setFrame(frame);

        settings = SettingsPanel.singleton();
        info = InfoPanel.singleton();
        menuPanel = MenuPanel.singleton();

        setTop(menuPanel);
        setCenter(imageContainer);
        setLeft(settings);
        setRight(info);

        setMargin(imageContainer, new Insets(50, 50, 50, 50));

        // Fade Animations for image/frame/background
        createAnimations();
    }

    public void createAnimations(){
        Duration duration = globalSettings.fadeDuration;

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

    public void achieveFocus(){
        backgroundHandler.setRoot(this);
        backgroundHandler.setCurrentBC(imageHandler.getBackgroundColor(getRealIndex(index)));
        backgroundHandler.resetNextBC();

        menuPanel.setMaxWidth(MAX_VALUE);
        menuPanel.setActive("slideshow");

        setTop(menuPanel);
        setLeft(settings);
        setRight(info);

        imageContainer.autosize();
        autosize();
    }

    public void select(int index) {
        preload(index);
    }

    public void preload(){
        preload(0);
    }

    public void preload(int index){
        assert(index < imageHandler.getFileCount());
        this.index = index;

        Vector<Integer> exceptions = new Vector<Integer>(preloadCount*2+1);
        for (int offset = -preloadCount; offset <= preloadCount; offset++){
            exceptions.add(getRealIndex(index+offset));
        }
        imageHandler.dropAll(exceptions);

        for (int offset = -preloadCount; offset <= preloadCount; offset++){
            if (offset == 0){
                // should be threaded and on threadFinish --> fadeIn || use scaled thumbnail
                imageHandler.preload(index);
                image.setImage(imageHandler.get(index));
                setCenter(imageContainer);
                imageFadeIn.play();
            }
            else
                imageHandler.preloadThreaded(getRealIndex(index+offset));
        }
    }


    public void next(){
        if (imageFade.statusProperty().get() != Animation.Status.STOPPED)
            return;
        backgroundHandler.setNextBC(imageHandler.getBackgroundColor(getRealIndex(index+1)));

        imageFade.play();
        frameFade.play();
        backgroundFade.play();

        increaseIndex();

        dropPreload(getRealIndex((index-1)-preloadCount), getRealIndex(index+preloadCount));
    }

    public void previous(){
        if (imageFade.statusProperty().get() != Animation.Status.STOPPED)
            return;
        backgroundHandler.setNextBC(imageHandler.getBackgroundColor(getRealIndex(index-1)));

        imageFade.play();
        frameFade.play();
        backgroundFade.play();

        reduceIndex();

        dropPreload(getRealIndex((index+1)+preloadCount), getRealIndex(index-preloadCount));
    }

    public void dropPreload(int dropIndex, int preloadIndex){
        if (backgroundThread != null) {
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        backgroundThread = new Thread(()->{
            imageHandler.drop(dropIndex);
            imageHandler.preloadThreaded(preloadIndex);
        });
        backgroundThread.start();
    }

    public void killBackgroundThread(){
        if (backgroundThread != null && backgroundThread.isAlive()){
            backgroundThread.interrupt();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void darkenBackground(){
        //backgroundHandler.
    }

    public void brightenBackground(){

    }

    public void slidePanels(){
        settings.slideInOut();
        info.slideInOut();
    }

    public void frameImage(){
        imageContainer.toggleFrame();
    }

    public void increaseIndex() {
        changeIndex(1);
    }

    public void reduceIndex() {
        changeIndex(-1);
    }

    public void changeIndex(int i) {
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

    public int getIndex() {
        return index;
    }
}
