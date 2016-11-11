package ViewFinder;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;


public class ViewFinder extends Application {
    private Stage primaryStage;
    private Scene currentScene;

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

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();

        startScreenLayout = new StartScreen();
        startScreenLayout.create();
        startScreenScene = new Scene(startScreenLayout, screenSize.getWidth(), screenSize.getHeight());

        galleryLayout = new Gallery();
        galleryLayout.create();
        galleryScene = new Scene(galleryLayout, screenSize.getWidth(), screenSize.getHeight());

        slideshowLayout = new Slideshow();
        slideshowLayout.create();
        slideshowScene = new Scene(slideshowLayout, screenSize.getWidth(), screenSize.getHeight());

        keyController = KeyController.singleton(this);
        keyController.setGallery(galleryLayout);
        keyController.setSlideshow(slideshowLayout);

        setupStage();

        currentScene = startScreenScene;
        //switchToStartScreen();

        switch(globalSettings.onStartAction){
            case "Default":
                newProject();
                //switchToStartScreen();
                //switchToSlideshow();
                break;
            case "Open":
                //open(globalSettings.getDefaultProject());
                //switchToGallery();
                break;
        }
    }

    public void setupStage(){
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);

        primaryStage.maximizedProperty().addListener((observableValue, was, now)-> {
            if (!now){
                Rectangle2D rect = Screen.getPrimary().getVisualBounds();
                primaryStage.setX(rect.getWidth()*0.1);
                primaryStage.setY(rect.getHeight()*0.1);

                primaryStage.setWidth(rect.getWidth()*0.8);
                primaryStage.setHeight(rect.getHeight()*0.8);
            }
        });

        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        if (!globalSettings.fullscreen)
            primaryStage.setMaximized(true);
        else
            primaryStage.setFullScreen(true);

        primaryStage.setOnCloseRequest(e -> exit());
    }

    public void switchToStartScreen(){
        keyController.setStartScreen(startScreenLayout);

        currentScene = startScreenScene;
        switchToLayout();

        startScreenScene.setOnKeyPressed(event -> keyController.handleStartScreen(event));
    }

    public void switchToSlideshow(){
        slideshowLayout.resetIndex();

        currentScene = slideshowScene;
        switchToLayout();

        slideshowScene.setOnKeyPressed(event -> keyController.handleSlideshow(event));
    }

    public void switchToGallery() {
        galleryLayout.resetIndex();

        currentScene = galleryScene;
        switchToLayout();

        galleryScene.setOnKeyPressed(event -> keyController.handleGallery(event));
    }

    // create a smoother transition between scenes...
    public void switchToLayout(){
        primaryStage.setScene(currentScene);
        if (!primaryStage.isShowing())
            primaryStage.show();
        if (primaryStage.isIconified())
            primaryStage.setIconified(false);
    }

    public void newProject(){
        primaryStage.setIconified(true);

        File imagePath = fileChooser.chooseImageFolder();
        if (imagePath == null){
            primaryStage.setIconified(false);
            return;
        }

        if (!imageHandler.isEmpty(imagePath)) {
            fileChooser.alertNoImages();
            primaryStage.setIconified(false);
            return;
        }

        String projectName = fileChooser.chooseProjectName(imagePath.getName());

        if (projectName.equals("")){
            //fileChooser.alertInvalidProjectName();
            primaryStage.setIconified(false);
            return;
        }

        globalSettings.newProject(projectName, imagePath);
        imageHandler.chooseDirectory(imagePath);

        galleryLayout.preload();
        slideshowLayout.preload();

        System.out.println("creating new Project: "+projectName);

        if (currentScene == startScreenScene)
            switchToSlideshow();
            //switchToGallery();
    }

    public void openProject(){
        primaryStage.setIconified(true);

        File projectPath = fileChooser.chooseExistingProject();
        if (projectPath == null){
            fileChooser.alertNoProject();
            primaryStage.setIconified(false);
            return;
        }

        //assert that imagePath and other variables are successfully read from globalSettings
        if (!globalSettings.openProject(projectPath.getName())){
            primaryStage.setIconified(false);
            return;
        }

        if (!imageHandler.chooseDirectory(globalSettings.imagePath)) {
            //should not happen since it is a already existing project, except if real images are no longer accessible
            fileChooser.alertMissingImages(); //--> offer to select new location of images
            primaryStage.setIconified(false);
            return;
        }
        galleryLayout.preload();
        slideshowLayout.preload();

        System.out.println("Opening existing Project: "+globalSettings.projectName);

        primaryStage.setIconified(false);
        if (currentScene == startScreenScene)
            switchToSlideshow();
            //switchToGallery();
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

