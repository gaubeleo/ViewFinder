package ViewFinder;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Vector;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Math.min;
import static java.lang.Math.max;

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

    private Rectangle selectZoom;
    private Rectangle startZoom;
    private Rectangle endZoom;

    private ZoomPos selectZoomPos;
    private ZoomPos startZoomPos;
    private ZoomPos endZoomPos;

    ////////////////////////////////////

    private ParallelTransition zoomTransition;
    private TranslateTransition zoomTranslation;
    private ScaleTransition zoomScale;

    private FadeTransition imageFadeIn;
    private FadeTransition imageFade;
    private FadeTransition fadeInFrame;
    private FadeTransition frameFade;

    private Transition backgroundFade;

    /////////   Development   //////////

    private boolean showZoom;
    private boolean nextEndZoom;

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

        showZoom = false;

        selectZoomPos = new ZoomPos();
        startZoomPos = new ZoomPos();
        endZoomPos = new ZoomPos();

        selectZoom = new Rectangle();
        selectZoom.setFill(Color.TRANSPARENT);
        selectZoom.setStrokeWidth(2);
        selectZoom.setStroke(Color.GREENYELLOW);
        selectZoom.widthProperty().bind(widthProperty().divide(selectZoomPos.scale));
        selectZoom.heightProperty().bind(heightProperty().divide(selectZoomPos.scale));
        selectZoom.translateXProperty().bind(imageContainer.widthProperty().divide(2).subtract(selectZoomPos.translateX).subtract(selectZoom.widthProperty().divide(2)));
        selectZoom.translateYProperty().bind(imageContainer.heightProperty().divide(2).subtract(selectZoomPos.translateY).subtract(selectZoom.heightProperty().divide(2)));
        selectZoom.setVisible(false);

        startZoom = new Rectangle();
        startZoom.setFill(Color.TRANSPARENT);
        startZoom.setStrokeWidth(2);
        startZoom.setStroke(Color.GREEN);
        startZoom.widthProperty().bind(widthProperty().divide(startZoomPos.scale));
        startZoom.heightProperty().bind(heightProperty().divide(startZoomPos.scale));
        startZoom.translateXProperty().bind(imageContainer.widthProperty().divide(2).subtract(startZoomPos.translateX).subtract(startZoom.widthProperty().divide(2)));
        startZoom.translateYProperty().bind(imageContainer.heightProperty().divide(2).subtract(startZoomPos.translateY).subtract(startZoom.heightProperty().divide(2)));
        startZoom.setVisible(false);

        endZoom = new Rectangle();
        endZoom.setFill(Color.TRANSPARENT);
        endZoom.setStrokeWidth(2);
        endZoom.setStroke(Color.RED);
        endZoom.widthProperty().bind(widthProperty().divide(endZoomPos.scale));
        endZoom.heightProperty().bind(heightProperty().divide(endZoomPos.scale));
        endZoom.translateXProperty().bind(imageContainer.widthProperty().divide(2).subtract(endZoomPos.translateX).subtract(endZoom.widthProperty().divide(2)));
        endZoom.translateYProperty().bind(imageContainer.heightProperty().divide(2).subtract(endZoomPos.translateY).subtract(endZoom.heightProperty().divide(2)));
        endZoom.setVisible(false);

        setOnMouseMoved(event -> {
            selectZoomPos.translateX.set(getWidth()/2 - event.getX());
            selectZoomPos.translateY.set(getHeight()/2 - event.getY());
        });

        setOnMouseDragged(event -> {
            selectZoomPos.translateX.set(getWidth()/2 - event.getX());
            selectZoomPos.translateY.set(getHeight()/2 - event.getY());

            if (!showZoom)
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
                startZoom.setVisible(true);
            }
            else{
                endZoomPos.set(selectZoomPos);
                endZoom.setVisible(true);
            }
        });

        // Same as MouseDragged
        setOnMousePressed(event -> {
            selectZoomPos.translateX.set(getWidth()/2 - event.getX());
            selectZoomPos.translateY.set(getHeight()/2 - event.getY());

            if (!showZoom)
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
                startZoom.setVisible(true);
            }
            else{
                endZoomPos.set(selectZoomPos);
                endZoom.setVisible(true);
            }
        });

        setOnScroll(event -> {
            if (!showZoom)
                return;

            // Test on different Mouse
            if (event.getDeltaY() > 0){
                selectZoomPos.scale.set(min(selectZoomPos.scale.doubleValue()*1.05, 15.));
            }
            else if (event.getDeltaY() < 0){
                selectZoomPos.scale.set(max(selectZoomPos.scale.doubleValue()*0.95, 1.));
            }
            else{
                System.out.println("WARNING: Unexpected MouseScroll DeltaY-value: 0");
            }

        });

        setOnMouseReleased(event -> {
            if (!showZoom)
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
                startZoom.setVisible(true);
                selectZoom.setStroke(Color.ORANGE);
                // uncomment to enable selectZoom rescale to endZoom
                //selectZoomPos.set(endZoomPos);
            }
            else{
                endZoomPos.set(selectZoomPos);
                endZoom.setVisible(true);
                selectZoom.setStroke(Color.GREENYELLOW);
                selectZoomPos.set(startZoomPos);
            }
        });


        setOnMouseExited(event -> {
            if (showZoom)
                selectZoom.setVisible(false);
        });

        setOnMouseEntered(event -> {
            if (showZoom)
                selectZoom.setVisible(true);
        });

        imageContainer.setZoomRects(selectZoom, startZoom, endZoom);

        //setTop(menuPanel);
        setCenter(imageContainer);
        setLeft(settings);
        setRight(info);

        setMargin(imageContainer, new Insets(50, 50, 50, 50));

        // Fade Animations for image/frame/background
        createAnimations();

    }

    public void createAnimations(){
        Duration duration = globalSettings.fadeDuration;

        zoomTranslation =  new TranslateTransition(new Duration(4500), imageContainer);
        zoomTranslation.setInterpolator(Interpolator.SPLINE(0.5, 0., 0.5, 1.));
        zoomScale =  new ScaleTransition(new Duration(4500), imageContainer);
        zoomScale.setInterpolator(Interpolator.SPLINE(0.5, 0., 0.5, 1.));

        zoomTransition = new ParallelTransition();
        zoomTransition.getChildren().addAll(zoomTranslation, zoomScale);
        zoomTransition.setOnFinished(event -> {
            imageContainer.translateXProperty().set(0);
            imageContainer.translateYProperty().set(0);

            imageContainer.setScaleX(1.);
            imageContainer.setScaleY(1.);
        });

        imageFadeIn = new FadeTransition(duration, image);
        imageFadeIn.setFromValue(0.0);
        imageFadeIn.setToValue(1.0);

        imageFade = new FadeTransition(duration, image);
        imageFade.setFromValue(1.0);
        imageFade.setToValue(0.0);
        imageFade.setOnFinished(e -> {
            image.setImage(imageHandler.get(index));
            imageFadeIn.play();

            startZoomPos.set(imageHandler.getStartZoom(index));
            endZoomPos.set(imageHandler.getEndZoom(index));

            selectZoom.setStroke(Color.GREENYELLOW);
            selectZoomPos.set(startZoomPos);
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

        showZoom = false;

        startZoomPos.set(imageHandler.getStartZoom(index));
        endZoomPos.set(imageHandler.getEndZoom(index));

        selectZoom.setStroke(Color.GREENYELLOW);
        selectZoomPos.set(startZoomPos);

        selectZoom.setVisible(false);
        startZoom.setVisible(false);
        endZoom.setVisible(false);

        menuPanel.setMaxWidth(MAX_VALUE);
        menuPanel.setActive("slideshow");

        //setTop(menuPanel);
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

    public void zoom(){
        if (zoomTransition.statusProperty().get() != Animation.Status.STOPPED)
            return;

        zoomScale.setFromX(startZoomPos.scale.doubleValue());
        zoomScale.setFromY(startZoomPos.scale.doubleValue());
        zoomTranslation.setFromX(startZoomPos.translateX.doubleValue()*startZoomPos.scale.doubleValue());
        zoomTranslation.setFromY(startZoomPos.translateY.doubleValue()*startZoomPos.scale.doubleValue());

        zoomScale.setToX(endZoomPos.scale.doubleValue());
        zoomScale.setToY(endZoomPos.scale.doubleValue());
        zoomTranslation.setToX(endZoomPos.translateX.doubleValue()*endZoomPos.scale.doubleValue());
        zoomTranslation.setToY(endZoomPos.translateY.doubleValue()*endZoomPos.scale.doubleValue());


        zoomTransition.play();
    }

    public void setZoom() {
        if (!showZoom) {
            selectZoomPos.scale.set(startZoomPos.scale.doubleValue());

            selectZoom.setVisible(true);
            startZoom.setVisible(true);
            endZoom.setVisible(true);

            showZoom = true;
        }
        else{
            selectZoom.setVisible(false);
            startZoom.setVisible(false);
            endZoom.setVisible(false);

            // reset selectZoom
            selectZoomPos.set(3., 0., 0.);

            showZoom = false;
        }
    }

    public int getIndex() {
        return index;
    }

    public static boolean isPortrait(Image img){
        return img.getHeight() > img.getWidth();
    }

    public static boolean isLandscape(Image img){
        return img.getHeight() < img.getWidth();
    }

    public static boolean isSquare(Image img){
        return img.getHeight() == img.getWidth();
    }

}
