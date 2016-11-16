package ViewFinder;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Vector;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Math.abs;

public class Gallery extends BorderPane{
    private final ViewFinder vf;
    private final GlobalSettings globalSettings;
    private final ImageHandler imageHandler;
    private BackgroundHandler backgroundHandler;
    private Thread backgroundThread;

    private SettingsPanel settings;
    private InfoPanel info;
    private MenuPanel menuPanel;

    ////////////////////////////////////

    private Rectangle fadeOut;
    private StackPane stackPane;
    private ScrollPane scrollPane;
    private ExpandedFlowPane flowLayout;

    private Vector<Thumbnail> thumbnails;

    ////////////////////////////////////

    private int index;

    ////////////////////////////////////

    Gallery(ViewFinder vf){
        this.vf = vf;
        this.index = -1;

        this.globalSettings = GlobalSettings.singleton();
        this.imageHandler = ImageHandler.singleton();

        thumbnails = new Vector<Thumbnail>(100);
    }

    public void create(){
        // Background and Frame Handler
        backgroundHandler =  BackgroundHandler.singleton(globalSettings.backgroundColor);

        flowLayout = new ExpandedFlowPane();
        flowLayout.prefWidthProperty().bind(widthProperty());

        flowLayout.setAlignment(Pos.BASELINE_CENTER);
        flowLayout.setPadding(new Insets(15, 65, 15, 65));
        flowLayout.setHgap(15);
        flowLayout.setVgap(15);


        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(flowLayout);
        //remove small border of scrollPane
        scrollPane.setStyle("-fx-background-color:transparent;");

        //fadeOut = new Rectangle(0, 0, 1900, 10);
        //Stop[] stops = {new Stop(0, Color.gray(1., 1.)), new Stop(1, Color.gray(1., 0.))};
        //LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        //fadeOut.setFill(grad);
        //fadeOut.setBlendMode(BlendMode.LIGHTEN);

        stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_CENTER);
        stackPane.getChildren().addAll(scrollPane); //, fadeOut

        settings = SettingsPanel.singleton();
        info = InfoPanel.singleton();
        menuPanel = MenuPanel.singleton();

        addWidthChangeListeners();

        setTop(menuPanel);
        setCenter(stackPane);
        setLeft(settings);
        setRight(info);
    }

    public void addWidthChangeListeners(){
        widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> menuPanel.setPrefWidth(newValue.doubleValue()));
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
        menuPanel.autosize();
        int fullRunLength = (int)(getScene().getWidth() - (settings.getWidth() + info.getWidth() + 2)); // +2 for scrollPane Border
        Platform.runLater(()->flowLayout.fitToWidth(fullRunLength));
    }

    public void newImageSet() {
        this.index = -1;
        flowLayout.clear();
        thumbnails.clear();
        System.gc();
        for (int i=0; i<imageHandler.getFileCount(); i++) {
            Thumbnail thumbnail = new Thumbnail(flowLayout, i);
            thumbnails.add(thumbnail);
        }
    }

    public void addThumbnailsThreaded(){
        backgroundThread = new Thread(() -> {
            for (int i=0; i< imageHandler.getFileCount(); i++){
                Thumbnail thumbnail = thumbnails.get(i);
                Image img = imageHandler.getThumbnail(i);
                if (img == null)
                    break;
                thumbnail.setImg(img);

                final int _i = i;
                thumbnail.setOnMouseClicked(e->{
                    if (e.getButton() == MouseButton.PRIMARY)
                        select(_i);
                        if (e.getClickCount() == 2)
                            vf.switchToSlideshow(_i);
                });

                Platform.runLater(()->flowLayout.addNode(thumbnail));
                thumbnail.show();
            }
        });
        backgroundThread.start();
    }

    public void killBackgroundThread(){
        if (backgroundThread != null && backgroundThread.isAlive()){
            backgroundThread.stop();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSize(){
        flowLayout.autosize();
        //flowLayout.fitToWidth();
        autosize();
    }

    public void achieveFocus(){
        backgroundHandler.setRoot(flowLayout);
        backgroundHandler.setCurrentBC(Color.gray(0.4));
        backgroundHandler.resetNextBC();

        menuPanel.setActive("gallery");

        setTop(menuPanel);
        setLeft(settings);
        setRight(info);

        updateSize();
    }

    public void select(int i) {
        if (i < 0 || i == index)
            return;
        assert(i < thumbnails.size());

        if (index >= 0)
            thumbnails.get(index).deselect();
        index = i;
        thumbnails.get(index).select();

        scrollTo(index);
    }

    public void preload(){
        preload(1);
    }

    public void preload(int threadCount){
        imageHandler.preloadThumbnailsThreaded(threadCount);
        newImageSet();
        addThumbnailsThreaded();
    }

    public void next(){
        select(getRealIndex(index+1));
    }

    public void previous(){
        select(getRealIndex(index-1));
    }

    public void down(){
        Bounds bounds = thumbnails.get(index).getBoundsInParent();
        double x = bounds.getMinX()+(bounds.getWidth()/2);
        double y = thumbnails.get(index).getParent().getBoundsInParent().getMinY();

        int indexBelow = index;
        double belowX, belowY, minDistance = MAX_VALUE;
        int i = index;

        do{
            if (i+1 == imageHandler.getFileCount() || thumbnails.get(i+1).getParent() == null)
                return;
            belowY = thumbnails.get(++i).getParent().getBoundsInParent().getMinY();
        } while(belowY == y);

        do{
            bounds = thumbnails.get(i).getBoundsInParent();
            belowX = bounds.getMinX()+(bounds.getWidth()/2);

            if (abs(belowX - x) < minDistance){
                minDistance = abs(belowX - x);
                indexBelow = i;
            }
            if (i+1 == imageHandler.getFileCount() || thumbnails.get(i+1).getParent() == null)
                break;
        } while(thumbnails.get(++i).getParent().getBoundsInParent().getMinY() == belowY && belowX < x);

        select(indexBelow);
    }

    public void up(){
        Bounds bounds = thumbnails.get(index).getBoundsInParent();
        double x = bounds.getMinX()+(bounds.getWidth()/2);
        double y = thumbnails.get(index).getParent().getBoundsInParent().getMinY();

        int indexAbove = index;
        double aboveX, aboveY, minDistance = MAX_VALUE;
        int i = index;
        do{
            if (i-1 == -1 || thumbnails.get(i-1).getParent() == null)
                return;
            aboveY = thumbnails.get(--i).getParent().getBoundsInParent().getMinY();
        } while(aboveY == y);

        do{
            bounds = thumbnails.get(i).getBoundsInParent();
            aboveX = bounds.getMinX()+(bounds.getWidth()/2);

            if (abs(aboveX - x) < minDistance){
                minDistance = abs(aboveX - x);
                indexAbove = i;
            }
            if (i-1 == -1 || thumbnails.get(i-1).getParent() == null)
                break;
        } while(thumbnails.get(--i).getParent().getBoundsInParent().getMinY() == aboveY && aboveX > x);

        select(indexAbove);
    }

    public void scrollTo(int i){
        if (thumbnails.get(i).getParent() == null)
            return;
        double offset = 15;
        Bounds bounds = thumbnails.get(i).getParent().getBoundsInParent();

        double vValue = scrollPane.getVvalue();
        double fullHeight = flowLayout.getHeight();
        double scrollHeight = scrollPane.getHeight();
        double curY = (fullHeight-scrollHeight)*vValue;

        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        if (minY-offset < curY) {
            double newVvalue = (minY-offset)/(fullHeight-scrollHeight);
            scrollPane.setVvalue(newVvalue);
        }
        else if (maxY+offset > curY+scrollHeight) {
            double newVvalue = (maxY + offset - scrollHeight) / (fullHeight - scrollHeight);
            scrollPane.setVvalue(newVvalue);
        }

//        System.out.println(bounds);
//        double value = (y-offset)/(flowLayout.getHeight()-scrollPane.getHeight());
//        scrollPane.setVvalue(min(value, scrollPane.getVmax()));
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
        select(0);
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

    public int getIndex() {
        return index;
    }
}

