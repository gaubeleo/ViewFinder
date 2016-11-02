package ViewFinder;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

import java.util.Vector;

public class Gallery extends BorderPane {

    private final GlobalSettings globalSettings;
    private final ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;

    private SettingsPanel settings;
    private InfoPanel info;

    ////////////////////////////////////

    protected ExpandedFlowPane flowLayout;

    protected Vector<ImageView> images;
    protected Vector<Rectangle> frames;

    ////////////////////////////////////

    private int index;

    ////////////////////////////////////

    Gallery(GlobalSettings globalSettings, ImageHandler imageHandler){
        this.index = 0;

        this.globalSettings = globalSettings;
        this.imageHandler = imageHandler;

        images = new Vector<ImageView>();
        frames = new Vector<Rectangle>();;

        create();
    }

    public void create(){
        // Background and Frame Handler
        backgroundHandler = new BackgroundHandler(this);

        ScrollPane scrollPane = new ScrollPane();

        flowLayout = new ExpandedFlowPane(scrollPane);
        flowLayout.prefWidthProperty().bind(widthProperty());

        flowLayout.setAlignment(Pos.BASELINE_LEFT);
        flowLayout.setPadding(new Insets(15, 15, 15, 15));
        flowLayout.setHgap(15);
        flowLayout.setVgap(15);

        images.clear();
        for (int i=0; i< imageHandler.getFileCount(); i++) {
            ImageView imageView = new ImageView(imageHandler.getThumbnail(i));
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            images.add(imageView);
            flowLayout.getChildren().add(imageView);
        }
        //addThumbnailsThreaded();
        //flowLayout.expandToWidth();

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(flowLayout);

        setCenter(scrollPane);
    }

    public void addThumbnailsThreaded(){
        Thread thread = new Thread(() -> {
            for (int i=0; i< imageHandler.getFileCount(); i++){
                ImageView imageView = images.get(i);

                FadeTransition fadeIn = new FadeTransition(globalSettings.fadeDuration, imageView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                ScaleTransition scaleIn = new ScaleTransition(globalSettings.fadeDuration, imageView);
                scaleIn.setFromX(0.7);
                scaleIn.setToX(1.0);
                scaleIn.setFromY(0.7);
                scaleIn.setToY(1.0);

                imageView.setImage(imageHandler.getThumbnail(i));
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                //fadeIn.play();
                //scaleIn.play();
            }
        });
        thread.start();
    }

    public void next(){
    }

    public void previous(){
    }

    public void darkenBackground(){
    }

    public void brightenBackground(){
    }

    public void slidePanels(){
        settings.slideInOut();
        info.slideInOut();
    }

    public void frameImages(){
        //
    }

    public void resetIndex(){
        index = 0;
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
            return imageHandler.getFileCount()+i;
        }
        else if (i >= imageHandler.getFileCount()){
            return i-imageHandler.getFileCount();
        }
        return i;
    }
}

