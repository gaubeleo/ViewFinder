package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

public class ViewFinder extends Application{
    private final String path = "H:\\Images\\Scotland - Isle of Skye\\fancy";
    private final int preloadCount = 2;

    ///////////////////////////

    private int index = 0;

    ///////////////////////////

    private KeyController keyController;

    private BorderPane slideshowLayout;
    private ImageHandler imageHandler;

    private ImageView slideshowImage;
    private Slideout settings;
    private Slideout info;

    private Vector<File> files;

    ///////////////////////////

    private FadeTransition fadeIn;
    private FadeTransition fadeOutIn;

    ///////////////////////////

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ViewFinder - Imagine");

        if (!chooseDirectory(new File(path))){
            System.exit(-1);
        }
        createSlideshow();
        createAnimations();

        Scene slideshowScene = new Scene(slideshowLayout, Color.GRAY);
        primaryStage.setScene(slideshowScene);

        // Keyboard Inputs are Handled by KeyController

        keyController = KeyController.singleton(this);
        slideshowScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                keyController.manageSlideshow(event);
            }
        });

        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.setMaximized(true);
        primaryStage.show();
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
        imageHandler = new ImageHandler(fileList);

        for (int offset = -preloadCount; offset <= preloadCount; offset++){
            imageHandler.preload_threaded(files.get(getRealIndex(index+offset)));
        }
        return true;
    }

    public void createSlideshow(){
        slideshowLayout = new BorderPane();
        slideshowLayout.setStyle("-fx-background-color: rgb(70, 70, 70);");

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

        ImageViewPane imageContainer = new ImageViewPane(slideshowImage);
        imageContainer.setImageView(slideshowImage);

        slideshowLayout.setCenter(imageContainer);
        slideshowLayout.setLeft(settings);
        slideshowLayout.setRight(info);

        slideshowLayout.setMargin(imageContainer, new Insets(50, 50, 50, 50));
    }

    public void createAnimations(){
        fadeIn = new FadeTransition(new Duration(250), slideshowImage);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOutIn = new FadeTransition(new Duration(250), slideshowImage);
        fadeOutIn.setFromValue(1.0);
        fadeOutIn.setToValue(0.0);
        fadeOutIn.setOnFinished(e -> {
            slideshowImage.setImage(imageHandler.get(files.get(index)));
            fadeIn.play();
        });
    }

    public void next(){
        fadeOutIn.play();
        increase_index();

        imageHandler.drop(files.get(getRealIndex((index-1)-preloadCount)));
        imageHandler.preload_threaded(files.get(getRealIndex(index+preloadCount)));
    }

    public void previous(){
        fadeOutIn.play();
        reduce_index();

        imageHandler.drop(files.get(getRealIndex((index+1)+preloadCount)));
        imageHandler.preload_threaded(files.get(getRealIndex(index-preloadCount)));
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

    public void slideInOut(){
        settings.slideInOut();
        info.slideInOut();
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
