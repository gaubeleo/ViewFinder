package ViewFinder;

import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Vector;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Math.abs;
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

    private Line zoomHorizontalHelper;
    private Line zoomVerticalHelper;
    private Circle zoomFixedPosHelper;

    private ZoomPos center;
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

    private SimpleBooleanProperty showZoom;
    private SimpleBooleanProperty zoomOnLine;
    private SimpleBooleanProperty zoomFixedPos;
    private SimpleBooleanProperty zoomOnCenter;
    private SimpleBooleanProperty mouseOutOfBounds;

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

    private void updateSelectZoomPos(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // ALT --> keep Zoom in Center
        if(zoomOnCenter.getValue()){
            if (!zoomOnLine.getValue()){
                selectZoomPos.translateX.set(0.);
                selectZoomPos.translateY.set(0.);
            }
            else{
                if (abs((getWidth()/2 - x) - center.translateX.doubleValue()) < abs((getHeight()/2 - y) - center.translateY.doubleValue())){
                    selectZoomPos.translateX.set(center.translateX.doubleValue());
                    selectZoomPos.translateY.set(getHeight()/2 - y);
                }
                else{
                    selectZoomPos.translateX.set(getWidth()/2 - x);
                    selectZoomPos.translateY.set(center.translateY.doubleValue());
                }
            }
        }
        // CTRL --> prohibit selectZoom movement
        else if (zoomFixedPos.getValue()) {
            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                selectZoomPos.translateX.set(endZoomPos.translateX.doubleValue());
                selectZoomPos.translateY.set(endZoomPos.translateY.doubleValue());
            }
            else{
                selectZoomPos.translateX.set(startZoomPos.translateX.doubleValue());
                selectZoomPos.translateY.set(startZoomPos.translateY.doubleValue());
            }
        }
        // SHIFT --> limit  selectZoom movement to horizontal or vertical line
        else if (zoomOnLine.getValue()){
            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                if (abs((getWidth()/2 - x) - endZoomPos.translateX.doubleValue()) < abs((getHeight()/2 - y) - endZoomPos.translateY.doubleValue())){
                    selectZoomPos.translateX.set(endZoomPos.translateX.doubleValue());
                    selectZoomPos.translateY.set(getHeight()/2 - y);
                }
                else{
                    selectZoomPos.translateX.set(getWidth()/2 - x);
                    selectZoomPos.translateY.set(endZoomPos.translateY.doubleValue());
                }
            }
            else{
                if (abs((getWidth()/2 - x) - startZoomPos.translateX.doubleValue()) < abs((getHeight()/2 - y) - startZoomPos.translateY.doubleValue())){
                    selectZoomPos.translateX.set(startZoomPos.translateX.doubleValue());
                    selectZoomPos.translateY.set(getHeight()/2 - y);
                }
                else{
                    selectZoomPos.translateX.set(getWidth()/2 - x);
                    selectZoomPos.translateY.set(startZoomPos.translateY.doubleValue());
                }
            }
        }
        // Default:
        else {
            selectZoomPos.translateX.set(getWidth()/2 - x);
            selectZoomPos.translateY.set(getHeight()/2 - y);
        }
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

        //setTop(menuPanel);
        setCenter(imageContainer);
        setLeft(settings);
        setRight(info);

        setMargin(imageContainer, new Insets(50, 50, 50, 50));

        // Fade Animations for image/frame/background
        createAnimations();

        /////////////////////////////////////////////////////////////

        showZoom = new SimpleBooleanProperty(false);
        zoomOnCenter = new SimpleBooleanProperty(false);
        zoomOnLine = new SimpleBooleanProperty(false);
        zoomFixedPos = new SimpleBooleanProperty(false);
        mouseOutOfBounds = new SimpleBooleanProperty(false);

        center = new ZoomPos(1., 0, 0);
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

        startZoom = new Rectangle();
        startZoom.setFill(Color.TRANSPARENT);
        startZoom.setStrokeWidth(2);
        startZoom.setStroke(Color.GREEN);
        startZoom.widthProperty().bind(widthProperty().divide(startZoomPos.scale));
        startZoom.heightProperty().bind(heightProperty().divide(startZoomPos.scale));
        startZoom.translateXProperty().bind(imageContainer.widthProperty().divide(2).subtract(startZoomPos.translateX).subtract(startZoom.widthProperty().divide(2)));
        startZoom.translateYProperty().bind(imageContainer.heightProperty().divide(2).subtract(startZoomPos.translateY).subtract(startZoom.heightProperty().divide(2)));

        endZoom = new Rectangle();
        endZoom.setFill(Color.TRANSPARENT);
        endZoom.setStrokeWidth(2);
        endZoom.setStroke(Color.RED);
        endZoom.widthProperty().bind(widthProperty().divide(endZoomPos.scale));
        endZoom.heightProperty().bind(heightProperty().divide(endZoomPos.scale));
        endZoom.translateXProperty().bind(imageContainer.widthProperty().divide(2).subtract(endZoomPos.translateX).subtract(endZoom.widthProperty().divide(2)));
        endZoom.translateYProperty().bind(imageContainer.heightProperty().divide(2).subtract(endZoomPos.translateY).subtract(endZoom.heightProperty().divide(2)));

        selectZoom.visibleProperty().bind(showZoom.and(mouseOutOfBounds.not()));
        startZoom.visibleProperty().bind(showZoom);
        endZoom.visibleProperty().bind(showZoom);

        imageContainer.setZoomRects(selectZoom, startZoom, endZoom);

        zoomHorizontalHelper = new Line(0, 500, 2000, 500);
        zoomHorizontalHelper.setStrokeWidth(2);
        zoomHorizontalHelper.setStroke(Color.BLACK);

        zoomVerticalHelper = new Line();
        zoomVerticalHelper.setStrokeWidth(2);
        zoomVerticalHelper.setStroke(Color.BLACK);

        zoomFixedPosHelper = new Circle();
        zoomFixedPosHelper.setStrokeWidth(15);
        zoomFixedPosHelper.setStroke(Color.BLACK);

        bindHelpers(endZoomPos);

        zoomHorizontalHelper.visibleProperty().bind(showZoom.and(zoomOnLine));
        zoomVerticalHelper.visibleProperty().bind(showZoom.and(zoomOnLine));
        zoomFixedPosHelper.visibleProperty().bind(showZoom.and(zoomFixedPos.or(zoomOnCenter)));

        imageContainer.setZoomHelpers(zoomHorizontalHelper, zoomVerticalHelper, zoomFixedPosHelper);

        setOnMouseMoved(event -> {
            updateSelectZoomPos(event);
        });

        setOnMouseDragged(event -> {
            updateSelectZoomPos(event);

            if (!showZoom.getValue())
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
            }
            else{
                endZoomPos.set(selectZoomPos);
            }
        });

        // Same as MouseDragged
        setOnMousePressed(event -> {
            if (!showZoom.getValue())
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
            }
            else{
                endZoomPos.set(selectZoomPos);
            }
        });

        setOnScroll(event -> {
            if (!showZoom.getValue())
                return;

            // Test on different Mouse
            if (event.getDeltaY() > 0){
                selectZoomPos.scale.set(min(selectZoomPos.scale.doubleValue()*1.05, 15.));
            }
            else if (event.getDeltaY() < 0){
                selectZoomPos.scale.set(max(selectZoomPos.scale.doubleValue()/1.05, 1.));
            }
            else{
                if (event.getDeltaX() > 0){
                    selectZoomPos.scale.set(min(selectZoomPos.scale.doubleValue()*1.05, 15.));
                }
                else if (event.getDeltaX() < 0){
                    selectZoomPos.scale.set(max(selectZoomPos.scale.doubleValue()*0.95, 1.));
                }
                else{
                    System.out.println("WARNING: Unexpected MouseScroll DeltaY-value: 0");
                }
            }

        });

        setOnMouseReleased(event -> {
            if (!showZoom.getValue())
                return;

            if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
                startZoomPos.set(selectZoomPos);
                selectZoom.setStroke(Color.ORANGE);
                // uncomment to enable selectZoom rescale to endZoom
                //selectZoomPos.set(endZoomPos);

                if (!zoomOnCenter.getValue()){
                    unbindHelpers();
                    bindHelpers(startZoomPos);
                }

                imageHandler.saveStartZoom(index, startZoomPos);
            }
            else{
                endZoomPos.set(selectZoomPos);
                selectZoom.setStroke(Color.GREENYELLOW);
                // uncomment to enable selectZoom rescale to startZoom
                //selectZoomPos.set(startZoomPos);

                if (!zoomOnCenter.getValue()){
                    unbindHelpers();
                    bindHelpers(endZoomPos);
                }

                imageHandler.saveEndZoom(index, endZoomPos);
            }
        });

        setOnMouseExited(event -> {
            mouseOutOfBounds.set(true);
        });

        setOnMouseEntered(event -> {
            mouseOutOfBounds.set(false);
        });

    }

    private void bindHelpers(ZoomPos zoomPos) {
        zoomHorizontalHelper.setStartX(-getMargin(imageContainer).getLeft());
        zoomHorizontalHelper.endXProperty().bind(widthProperty());
        zoomHorizontalHelper.startYProperty().bind(imageContainer.heightProperty().divide(2).subtract(zoomPos.translateY));
        zoomHorizontalHelper.endYProperty().bind(imageContainer.heightProperty().divide(2).subtract(zoomPos.translateY));

        zoomVerticalHelper.startXProperty().bind(imageContainer.widthProperty().divide(2).subtract(zoomPos.translateX));
        zoomVerticalHelper.endXProperty().bind(imageContainer.widthProperty().divide(2).subtract(zoomPos.translateX));
        zoomVerticalHelper.setStartY(-getMargin(imageContainer).getTop());
        zoomVerticalHelper.endYProperty().bind(heightProperty());

        zoomFixedPosHelper.centerXProperty().bind(imageContainer.widthProperty().divide(2).subtract(zoomPos.translateX));
        zoomFixedPosHelper.centerYProperty().bind(imageContainer.heightProperty().divide(2).subtract(zoomPos.translateY));
    }

    private void bindHelpersToCenter(){
        zoomHorizontalHelper.setStartX(0.0);
        zoomHorizontalHelper.endXProperty().bind(widthProperty());
        zoomHorizontalHelper.startYProperty().bind(imageContainer.heightProperty().divide(2).subtract(center.translateY));
        zoomHorizontalHelper.endYProperty().bind(imageContainer.heightProperty().divide(2).subtract(center.translateY));

        zoomVerticalHelper.startXProperty().bind(imageContainer.widthProperty().divide(2).subtract(center.translateX));
        zoomVerticalHelper.endXProperty().bind(imageContainer.widthProperty().divide(2).subtract(center.translateX));
        zoomVerticalHelper.setStartY(0);
        zoomVerticalHelper.endYProperty().bind(heightProperty());

        zoomFixedPosHelper.centerXProperty().bind(imageContainer.widthProperty().divide(2).subtract(center.translateX));
        zoomFixedPosHelper.centerYProperty().bind(imageContainer.heightProperty().divide(2).subtract(center.translateY));
    }

    private void unbindHelpers() {
        zoomHorizontalHelper.endXProperty().unbind();
        zoomHorizontalHelper.startYProperty().unbind();
        zoomHorizontalHelper.endYProperty().unbind();

        zoomVerticalHelper.startXProperty().unbind();
        zoomVerticalHelper.endXProperty().unbind();
        zoomVerticalHelper.endYProperty().unbind();

        zoomFixedPosHelper.centerXProperty().unbind();
        zoomFixedPosHelper.centerYProperty().unbind();
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

        showZoom.set(false);
        zoomOnCenter.set(false);
        zoomOnLine.set(false);
        zoomFixedPos.set(false);

        startZoomPos.set(imageHandler.getStartZoom(index));
        endZoomPos.set(imageHandler.getEndZoom(index));

        selectZoom.setStroke(Color.GREENYELLOW);
        selectZoomPos.set(startZoomPos);

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
        if (!showZoom.getValue()) {
            selectZoomPos.scale.set(startZoomPos.scale.doubleValue());

            showZoom.set(true);
        }
        else{
            showZoom.set(false);
        }
    }

    public void lockZoomOnCenter(){
        zoomOnCenter.set(true);

        unbindHelpers();
        bindHelpersToCenter();

        selectZoomPos.set(selectZoomPos.scale.doubleValue(), 0, 0);
    }

    public void lockZoomOnLine() {
        zoomOnLine.set(true);

        // TODO: move selectZoom to MousePos on Line
    }

    public void lockZoomFixedPos() {
        zoomFixedPos.set(true);

        if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
            selectZoomPos.translateX.set(endZoomPos.translateX.doubleValue());
            selectZoomPos.translateY.set(endZoomPos.translateY.doubleValue());
        }
        else{
            selectZoomPos.translateX.set(startZoomPos.translateX.doubleValue());
            selectZoomPos.translateY.set(startZoomPos.translateY.doubleValue());
        }
    }

    public void releaseZoomOnCenter() {
        zoomOnCenter.set(false);

        unbindHelpers();
        if (selectZoom.getStroke().equals(Color.GREENYELLOW)){
            bindHelpers(endZoomPos);
        }
        else{
            bindHelpers(startZoomPos);
        }

        // TODO: move selectZoom to correct MousePos
    }

    public void releaseZoomOnLine() {
        zoomOnLine.set(false);

        // TODO: move selectZoom to correct MousePos
    }

    public void releaseZoomFixedPos() {
        zoomFixedPos.set(false);

        // TODO: move selectZoom to correct MousePos
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
