package ViewFinder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;


public class ViewFinder extends Application {
    private Stage primaryStage;
    private Scene currentScene;
    private Pane currentLayout;

    private Scene startScreenScene;
    private Scene slideshowScene;
    private Scene galleryScene;

    private StartScreen startScreenLayout;
    private Slideshow slideshowLayout;
    private Gallery galleryLayout;

    private GlobalSettings globalSettings;
    private FileChooser fileChooser;
    private KeyController keyController;
    private ImageHandler imageHandler;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("ViewFinder - Imagine");

        globalSettings = GlobalSettings.singleton();
        if (!globalSettings.load())
            globalSettings.save();

        fileChooser = new FileChooser();
        imageHandler = ImageHandler.singleton();
        keyController = KeyController.singleton(this);
        setupStage();

        switch(globalSettings.onStartAction){
            case "Default":
                //newProject();
                switchToStartScreen();
                //switchToSlideshow();
                break;
            case "Open":
                //open(globalSettings.getDefaultProject());
                //switchToGallery();
                break;
        }
    }

    public void setupStage(){
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);

        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        if (!globalSettings.fullscreen)
            primaryStage.setMaximized(true);
        else
            primaryStage.setFullScreen(true);

        primaryStage.setOnCloseRequest(e -> exit());
    }


    public void switchToStartScreen(){
        if (startScreenScene == null){
            startScreenLayout = new StartScreen();
            startScreenLayout.create();
            startScreenScene = new Scene(startScreenLayout);
        }
        keyController.setStartScreen(startScreenLayout);

        currentLayout = startScreenLayout;
        currentScene = startScreenScene;
        switchToLayout();
        startScreenScene.setOnKeyPressed(event -> keyController.handleStartScreen(event));
    }

    public void switchToSlideshow(){
        if (slideshowScene == null){
            slideshowLayout = new Slideshow(globalSettings, imageHandler);
            slideshowLayout.create();
            slideshowScene = new Scene(slideshowLayout);
        }
        slideshowLayout.resetIndex();
        slideshowLayout.preload();
        keyController.setSlideshow(slideshowLayout);

        currentLayout = slideshowLayout;
        currentScene = slideshowScene;
        switchToLayout();

        slideshowScene.setOnKeyPressed(event -> keyController.handleSlideshow(event));
    }

    // create a smoother transition between scenes...
    private void switchToLayout(){
        primaryStage.setScene(currentScene);
        if (!primaryStage.isShowing())
            primaryStage.show();
        if (primaryStage.isIconified())
            primaryStage.setIconified(false);
    }

    public void newProject(){
        primaryStage.setIconified(true);

        File imagePath = fileChooser.chooseImageFolder();
        if (imagePath == null)
            return;

        if (!imageHandler.chooseDirectory(imagePath)) {
            fileChooser.allertNoImages();
            return;
        }

        String projectName = fileChooser.chooseProjectName(imagePath.getName());

        if (!projectName.equals("")){
            System.out.println("creating new Project: "+projectName);
            globalSettings.newProject(projectName, imagePath);

            switchToSlideshow();
            //switchToGallery();

        }
    }

    public void openProject(){

    }

    public void toggleFullscreen(){
        if (!globalSettings.fullscreen)
            primaryStage.setFullScreen(true);
        else
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(true);
        globalSettings.fullscreen = !globalSettings.fullscreen;
    }

    public void exit(){
        System.out.println("\nexiting Viewfinder - Imagine");
        System.exit(0);
    }
}

