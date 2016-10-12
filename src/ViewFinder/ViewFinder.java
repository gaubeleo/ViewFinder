package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;
import java.util.Vector;

public class ViewFinder extends Application{
    private final String path = "H:\\Images\\Scotland - Isle of Skye\\fancy";
    private boolean fullscreen = false;
    private final int preloadCount = 2;
    private int frameWidth = 3;
    private final Duration fadeDuration = new Duration(800);

    ///////////////////////////

    private int index = 0;

    ///////////////////////////

    private Settings globalSettings;
    private KeyController keyController;

    private Stage primaryStage;
    private BorderPane startLayout;
    private BorderPane slideshowLayout;
    private BorderPane galleryLayout;
    private ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;

    private VBox welcomeScreen;
    private Rectangle frame;
    private ImageViewPane imageContainer;
    private ImageView slideshowImage;
    private Slideout settings;
    private Slideout info;

    private Vector<File> files;

    private Random random = new Random();

    ///////////////////////////

    private FadeTransition fadeIn;
    private FadeTransition fadeOutIn;
    private FadeTransition fadeInFrame;
    private FadeTransition fadeOutInFrame;

    private Transition fadeBC;

    ///////////////////////////

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("ViewFinder - Imagine");

        globalSettings = new Settings("Default", Color.gray(0.25));
        if (!globalSettings.load())
            globalSettings.save();

        keyController = KeyController.singleton(this);
        imageHandler = new ImageHandler();
        if (!chooseDirectory(new File(path))){
            System.exit(-1);
        }
        createStartScreen();
        //createSlideshow();
        //createAnimations();

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

    public void createStartScreen(){
        startLayout = new BorderPane();

        createSettingsPanel();
        createInfoPanel();

        welcomeScreen = new VBox();

        Label headline = new Label("Viewfinder - Imagine");
        Label[] shortcuts = {new Label("Press Ctrl + N to start a new project"), new Label("Press Ctrl + O to open an existing project")};


        welcomeScreen.getChildren().add(headline);
        welcomeScreen.getChildren().addAll(shortcuts);

        startLayout.setCenter(welcomeScreen);
        startLayout.setLeft(settings);
        startLayout.setRight(info);

        startLayout.setMargin(welcomeScreen, new Insets(100, 100, 100, 100));
    }

    public void createSlideshow(){
        slideshowLayout = new BorderPane();

        createSettingsPanel();
        createInfoPanel();

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
        frame = backgroundHandler.createFrame(frameWidth);
    }

    public void createInfoPanel(){
        if (info != null)
            return;

        Label infoLabel = new Label("Image Info");
        info = new Slideout(200, Pos.BASELINE_RIGHT, infoLabel);
        info.setStyle("-fx-background-color: rgb(100, 100, 100);");
        info.setPadding(new Insets(25, 25, 25, 25));
    }

    public void createSettingsPanel(){
        if (settings != null)
            return;

        // Replace label with Class that inherits from VBox
        Label settingsLabel = new Label("Global Settings");
        settings = new Slideout(200, Pos.BASELINE_LEFT, settingsLabel);
        settings.setStyle("-fx-background-color: rgb(100, 100, 100);");
        settings.setPadding(new Insets(25, 25, 25, 25));
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
            backgroundHandler.adjustFrameColor();
            fadeInFrame.play();
        });

        fadeBC = backgroundHandler.fadeBackground(fadeDuration.multiply(2));
    }

    public void setupScene(){
        Scene slideshowScene = new Scene(startLayout);
        
        // Keyboard Inputs are Handled by KeyController
        slideshowScene.setOnKeyPressed(event -> keyController.manageSlideshow(event));

        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);

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
        backgroundHandler.setNextBC(Color.gray(random.nextDouble()));
        fadeOutIn.play();
        fadeOutInFrame.play();
        fadeBC.play();
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
