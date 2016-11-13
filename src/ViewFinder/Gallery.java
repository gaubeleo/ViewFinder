package ViewFinder;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;

import java.util.Vector;

public class Gallery extends BorderPane{

    private final GlobalSettings globalSettings;
    private final ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;

    private SettingsPanel settings;
    private InfoPanel info;
    private MenuPanel menuPanel;

    ////////////////////////////////////

    private ScrollPane scrollPane;
    private ExpandedFlowPane flowLayout;

    private Vector<Thumbnail> thumbnails;
    private Vector<Rectangle> frames;

    ////////////////////////////////////

    private int index;

    ////////////////////////////////////

    Gallery(){
        this.index = 0;

        this.globalSettings = GlobalSettings.singleton();
        this.imageHandler = ImageHandler.singleton();

        thumbnails = new Vector<Thumbnail>(100);
        frames = new Vector<Rectangle>(100);
    }

    public void create(){
        // Background and Frame Handler
        backgroundHandler =  BackgroundHandler.singleton(globalSettings.backgroundColor);

        scrollPane = new ScrollPane();

        flowLayout = new ExpandedFlowPane();
        flowLayout.prefWidthProperty().bind(widthProperty());

        flowLayout.setAlignment(Pos.BASELINE_CENTER);
        flowLayout.setPadding(new Insets(15, 65, 15, 65));
        flowLayout.setHgap(15);
        flowLayout.setVgap(15);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(flowLayout);
        scrollPane.setStyle("-fx-background-color:transparent;");

        settings = SettingsPanel.singleton();
        info = InfoPanel.singleton();
        menuPanel = MenuPanel.singleton();

        addWidthChangeListeners();

        setTop(menuPanel);
        setCenter(scrollPane);
        setLeft(settings);
        setRight(info);
    }

    public void addWidthChangeListeners(){
        widthProperty().addListener((observable, oldValue, newValue) -> {
            fitToWidth();
        });
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            fitToWidth();
        });
        settings.widthProperty().addListener((observable, oldValue, newValue) -> {
            fitToWidth();
        });
        info.widthProperty().addListener((observable, oldValue, newValue) -> {
            fitToWidth();
        });
    }

    public void fitToWidth(){
        if (getScene() == null)
            return;
        double fullRunLength = getScene().getWidth() - (settings.getWidth() + info.getWidth());
        Platform.runLater(()->flowLayout.fitToWidth(fullRunLength));
    }

    public void newImageSet(){
        flowLayout.clear();
        thumbnails.clear();
        frames.clear();
        for (int i=0; i<imageHandler.getFileCount(); i++) {
            thumbnails.add(new Thumbnail(flowLayout));
        }
    }

    public void addThumbnailsThreaded(){
        newImageSet();
        new Thread(() -> {
            for (int i=0; i< imageHandler.getFileCount(); i++){
                Thumbnail thumbnail = thumbnails.get(i);
                thumbnail.setImg(imageHandler.getThumbnail(i));
                Platform.runLater(()->flowLayout.addNode(thumbnail));
                thumbnail.show();
            }
        }).start();
    }

    public void updateSize(){
        //flowLayout.autosize();
        //flowLayout.fitToWidth();
        autosize();
    }

    public void achieveFocus(){
        backgroundHandler.setRoot(flowLayout);
        backgroundHandler.setCurrentBC(Color.WHITE);
        backgroundHandler.setNextBC(Color.WHITE);

        menuPanel.setActive("gallery");

        setTop(menuPanel);
        setLeft(settings);
        setRight(info);

        updateSize();
    }

    public void preload(){
        preload(2);
    }

    public void preload(int threadCount){
        resetIndex();
        imageHandler.preloadThumbnailsThreaded(threadCount);
        addThumbnailsThreaded();
    }

    public void next(){
    }

    public void previous(){
    }

    public void scrollUp(){
        double scrollSpeed = 0.05 / (flowLayout.getHeight()/scrollPane.getHeight());
        scrollPane.setVvalue(max(scrollPane.getVvalue()-scrollSpeed, scrollPane.getVmin()));
    }

    public void scrollDown(){
        double scrollSpeed = 0.05 / (flowLayout.getHeight()/scrollPane.getHeight());
        scrollPane.setVvalue(min(scrollPane.getVvalue()+scrollSpeed, scrollPane.getVmax()));

    }

    private static double max(double x, double y) {
        return x>y ? x : y;
    }

    private static double min(double x, double y) {
        return x<y ? x : y;
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

