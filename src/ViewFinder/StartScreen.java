package ViewFinder;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class StartScreen extends BorderPane {

    private VBox welcomeScreen;
    private SettingsPanel settings;
    private InfoPanel info;

    public StartScreen(){}

    public void create(){
        welcomeScreen = new VBox();

        Label headline = new Label("Viewfinder - Imagine");
        Label[] shortcuts = {new Label("Press Ctrl + N to start a new project"), new Label("Press Ctrl + O to open an existing project")};

        welcomeScreen.getChildren().add(headline);
        welcomeScreen.getChildren().addAll(shortcuts);

        settings = SettingsPanel.singleton();
        info = InfoPanel.singleton();

        setCenter(welcomeScreen);
        setLeft(settings);
        setRight(info);

        setMargin(welcomeScreen, new Insets(100, 100, 100, 100));
    }

    public void slidePanels(){
        settings.slideInOut();
        info.slideInOut();
    }
}
