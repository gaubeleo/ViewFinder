package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

public class ViewFinder extends Application{
    private final String path = "H:\\Images\\Scotland - Isle of Skye\\fancy";
    private boolean fullscreen = false;
    private final int preloadCount = 2;
    private int frameWidth = 3;
    private final Duration fadeDuration = new Duration(300);

    ///////////////////////////

    private int index = 0;

    ///////////////////////////

    private Settings globalSettings;
    private KeyController keyController;

    private Stage primaryStage;
    private BorderPane slideshowLayout;
    private ImageHandler imageHandler;

    private Rectangle frame;
    private ImageViewPane imageContainer;
    private ImageView slideshowImage;
    private Slideout settings;
    private Slideout info;

    private Vector<File> files;

    ///////////////////////////

    private FadeTransition fadeIn;
    private FadeTransition fadeOutIn;
    private FadeTransition fadeInFrame;
    private FadeTransition fadeOutInFrame;
    private BackgroundHandler backgroundHandler;

    ///////////////////////////

    public final double WHITE = 1.;
    public final double BLACK = 0.;


    ///////////////////////////

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("ViewFinder - Imagine");

        globalSettings = new Settings(Color.gray(WHITE));
        keyController = KeyController.singleton(this);
        imageHandler = new ImageHandler();
        if (!chooseDirectory(new File(path))){
            System.exit(-1);
        }
        createFrame();
        createSlideshow();
        createAnimations();

        //JavaFX Stage/Scene
        setupScene();
    }

    public boolean chooseDirectory(File path){
        files = new Vector<File>();

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

        for (File file : fileList){
            files.add(file);
        }
        imageHandler.addAll(fileList);

        for (int offset = -preloadCount; offset <= preloadCount; offset++){
            imageHandler.preload_threaded(files.get(getRealIndex(index+offset)));
        }
        return true;
    }

    public void createFrame(){
        frame = new Rectangle();

        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(Color.gray(BLACK));
        // +2: weird bug fix on edge between image and frame
        frame.setStrokeWidth(frameWidth + 2);
    }

    public void createSlideshow(){
        slideshowLayout = new BorderPane();
        //slideshowLayout.setStyle("-fx-background-color: #CCFF99;");

        // Replace label with Class that inherits from VBox
        Label settingsLabel = new Label("Global Settings");
        settings = new Slideout(300, Pos.BASELINE_LEFT, settingsLabel);
        settings.setStyle("-fx-background-color: rgb(100, 100, 100);");
        settings.setPadding(new Insets(25, 25, 25, 25));

        Label infoLabel = new Label("Image Info");
        info = new Slideout(300, Pos.BASELINE_RIGHT, infoLabel);
        info.setStyle("-fx-background-color: rgb(100, 100, 100);");
        info.setPadding(new Insets(25, 25, 25, 25));

        slideshowImage = new ImageView();
        slideshowImage.setPreserveRatio(true);
        slideshowImage.setSmooth(true);
        slideshowImage.setImage(imageHandler.get(files.get(index)));
        slideshowImage.setCache(true);
        slideshowImage.setCacheHint(CacheHint.SCALE);

        imageContainer = new ImageViewPane(slideshowImage);
        imageContainer.setImageView(slideshowImage);

        slideshowLayout.setCenter(imageContainer);
        slideshowLayout.setLeft(settings);
        slideshowLayout.setRight(info);

        slideshowLayout.setMargin(imageContainer, new Insets(50, 50, 50, 50));

        backgroundHandler = new BackgroundHandler(slideshowLayout, globalSettings.backgroundColor);
    }

    public void createAnimations(){
        fadeIn = new FadeTransition(fadeDuration, slideshowImage);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOutIn = new FadeTransition(fadeDuration, slideshowImage);
        fadeOutIn.setFromValue(1.0);
        fadeOutIn.setToValue(0.0);
        fadeOutIn.setOnFinished(e -> {
            slideshowImage.setImage(imageHandler.get(files.get(index)));
            fadeIn.play();
        });

        fadeInFrame = new FadeTransition(fadeDuration, frame);
        fadeInFrame.setFromValue(0.0);
        fadeInFrame.setToValue(1.0);

        fadeOutInFrame = new FadeTransition(fadeDuration, frame);
        fadeOutInFrame.setFromValue(1.0);
        fadeOutInFrame.setToValue(0.0);
        fadeOutInFrame.setOnFinished(e -> {
            fadeInFrame.play();
        });

        backgroundHandler.setAnimationDuration(fadeDuration.multiply(2));
        backgroundHandler.setOnFinished(e -> {
            backgroundHandler.transitionFinished();
        });
        backgroundHandler.setNextBC(Color.gray(BLACK));
    }

    public void setupScene(){
        Scene slideshowScene = new Scene(slideshowLayout);
        
        // Keyboard Inputs are Handled by KeyController
        slideshowScene.setOnKeyPressed(event -> keyController.manageSlideshow(event));

        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);

        //primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        if (!fullscreen)
            primaryStage.setMaximized(true);
        else
            primaryStage.setFullScreen(true);

        primaryStage.setScene(slideshowScene);
        primaryStage.show();
    }

    public void next(){
        fadeOutIn.play();
        fadeOutInFrame.play();
        backgroundHandler.play();
        increase_index();

        imageHandler.drop(files.get(getRealIndex((index-1)-preloadCount)));
        imageHandler.preload_threaded(files.get(getRealIndex(index+preloadCount)));
    }

    public void previous(){
        fadeOutIn.play();
        fadeOutInFrame.play();
        reduce_index();

        imageHandler.drop(files.get(getRealIndex((index+1)+preloadCount)));
        imageHandler.preload_threaded(files.get(getRealIndex(index-preloadCount)));
    }

    public void slideInOut(){
        settings.slideInOut();
        info.slideInOut();
    }

    public void addRemoveFrame(){
        if (imageContainer.isFramed())
            imageContainer.removeFrame();
        else{
            imageContainer.setFrame(frame);
        }
    }

    public void toggleFullscreen(){
        if (!fullscreen)
            primaryStage.setFullScreen(true);
        else
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(true);
        fullscreen = !fullscreen;
    }

    public void exit(){
        System.out.println("exiting Viewfinder - Imagine...");
        System.exit(0);
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
            return files.size()+i;
        }
        else if (i >= files.size()){
            return i-files.size();
        }
        return i;
    }
}
