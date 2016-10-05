package ViewFinder;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by Leo on 05.10.2016.
 */
public class ViewFinder extends Application{
    private BorderPane slideshowLayout;

    private int index = 0;
    private boolean sliders = false;

    static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ViewFinder - Imagine");

        createSlideshow();

        Scene scene = new Scene(slideshowLayout);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public void createSlideshow(){
        slideshowLayout = new BorderPane();
        ImageViewPane imageContainer = new ImageViewPane();


        Label settingsLabel = new Label("Global Settings");
        Slideout settings = new Slideout(300, Pos.BASELINE_CENTER, settingsLabel);


        slideshowLayout.setCenter(imageContainer);
        slideshowLayout.setLeft(settings);
        //slideshowLayout.setRight();
    }

    public void next(){
        index += 1;
    }

    public void previous(){
        index -= 1;
    }

    public void slideInOut(){
        if (sliders){
            slideIn();
        }
        else{
            slideOut();
        }
    }

    public void slideIn(){

    }

    public void slideOut(){

    }
}
