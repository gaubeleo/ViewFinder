package ViewFinder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

public class ViewFinder extends Application {
    private Stage primaryStage;
    private Parent currentLayout;

    private Scene currentScene;

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

        //SettingsPanel settingsPanel = SettingsPanel.singleton();
        //InfoPanel infoPanel = InfoPanel.singleton();
        MenuPanel menuPanel = MenuPanel.singleton();
        menuPanel.setViewFinder(this);

        startScreenLayout = new StartScreen();
        startScreenLayout.create();

        galleryLayout = new Gallery(this);
        galleryLayout.create();

        slideshowLayout = new Slideshow(this);
        slideshowLayout.create();

        keyController = KeyController.singleton(this);
        keyController.setGallery(galleryLayout);
        keyController.setSlideshow(slideshowLayout);

        //switchToStartScreen();

        setupStage();

        switch(globalSettings.onStartAction){
            case "Default":
                //newProject();
                if (globalSettings.mostRecentProject.getName().compareTo(".") == 0)
                    switchToStartScreen();
                else
                    openProject(globalSettings.mostRecentProject);
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
        primaryStage.setMinHeight(800);
        primaryStage.setMinWidth(1200);

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
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.valueOf("P"));

        if (!globalSettings.fullscreen)
            primaryStage.setMaximized(true);
        else
            primaryStage.setFullScreen(true);

        primaryStage.setOnCloseRequest(e -> exit());

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();

        //should be currentLayout!!!!!!
        currentScene = new Scene(startScreenLayout, screenSize.getWidth(), screenSize.getHeight());

        primaryStage.setScene(currentScene);
    }

    public void switchToStartScreen(){
        if (currentLayout == startScreenLayout)
            return;
        keyController.setStartScreen(startScreenLayout);

        currentLayout = startScreenLayout;
        switchToLayout();

        currentScene.setOnKeyPressed(event -> keyController.handleStartScreenPressed(event));
    }

    public void switchToSlideshow(){
        switchToSlideshow(0);
    }

    public void switchToSlideshow(int index){
        slideshowLayout.select(index);
        if (currentLayout == slideshowLayout)
            return;
        slideshowLayout.achieveFocus();

        currentLayout = slideshowLayout;
        switchToLayout();

        currentScene.setOnKeyPressed(event -> keyController.handleSlideshowPressed(event));
        currentScene.setOnKeyReleased(event -> keyController.handleSlideshowReleased(event));
    }

    public void switchToGallery(){
        switchToGallery(0);
    }

    public void switchToGallery(int index) {
        galleryLayout.select(index);
        if (currentLayout == galleryLayout)
            return;
        galleryLayout.achieveFocus();

        currentLayout = galleryLayout;
        switchToLayout();

        currentScene.setOnKeyPressed(event -> keyController.handleGalleryPressed(event));
    }

    // create a smoother transition between scenes...
    public void switchToLayout(){
        primaryStage.getScene().setRoot(currentLayout);
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

        //catch warning overwriting project!! --> copy to last Overwritten-Project in Backups
        if (projectName.equals("")){
            //fileChooser.alertInvalidProjectName();
            primaryStage.setIconified(false);
            return;
        }
        globalSettings.newProject(projectName, imagePath);
        System.out.println("creating new Project: "+projectName);

        openProject(new File(projectName));
    }

    public void openProject(){
        primaryStage.setIconified(true);

        File projectPath = fileChooser.chooseExistingProject();
        if (projectPath == null){
            //fileChooser.alertNoProject();
            primaryStage.setIconified(false);
            return;
        }
        openProject(projectPath);
    }

    void openProject(File projectPath){
        //assert that imagePath and other variables are successfully read from globalSettings
        if (!primaryStage.isIconified()){
            primaryStage.setIconified(true);
        }

        if (!globalSettings.openProject(projectPath.getName())){
            fileChooser.alertNoProject();
            primaryStage.setIconified(false);
            return;
        }

//        if (!imageHandler.chooseDirectory(globalSettings.imagePath)) {
//            //should not happen since it is a already existing project, except if real images are no longer accessible
//            fileChooser.alertMissingImages(); //--> offer to select new location of images
//            primaryStage.setIconified(false);
//            return;
//        }
        globalSettings.updateMostRecentProject(projectPath);

        galleryLayout.killBackgroundThread();
        slideshowLayout.killBackgroundThread();
        imageHandler.chooseDirectory(globalSettings.imagePath);

        galleryLayout.preload();
        slideshowLayout.preload();

        System.out.println("Opening Project: "+globalSettings.projectName);

        primaryStage.setIconified(false);

        //if (currentLayout == null || currentLayout == startScreenLayout)
        //switchToSlideshow();
        switchToGallery();
    }


    public void toggleFullscreen(){
        if (!globalSettings.fullscreen) {
            //MenuPanel.singleton().setVisible(false);
            primaryStage.setFullScreen(true);
        }
        else {
            primaryStage.setFullScreen(false);
            //MenuPanel.singleton().setVisible(true);
            primaryStage.setMaximized(true);
        }
        globalSettings.fullscreen = !globalSettings.fullscreen;
    }

    public void exit(){
        System.out.println("\nexiting Viewfinder - Imagine");
        System.exit(0);
    }
}

