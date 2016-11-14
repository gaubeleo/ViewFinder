package ViewFinder;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static java.lang.Math.abs;

class Run extends HBox {
    private int size;
    private double totalSpacing;
    private double minRunLength;
    private double curRunLength;
    private double oldRunLength;
    private double maxRunLength;
    private double minHeight;
    private double curHeight;
    private Run nextRun;


    public Run(double maxRunLength, double minHeight, double Vgap){
        super();

        this.size = 0;
        this.totalSpacing = 0.;
        this.minRunLength = 0.;
        this.curRunLength = 0.;
        this.oldRunLength = 0.;
        this.maxRunLength = maxRunLength;
        this.minHeight = minHeight;

        setSpacing(Vgap);
    }

    public synchronized boolean addNode(Thumbnail thumbnail){
        return addNode(size, thumbnail);
    }

    public synchronized boolean addNode(int index, Thumbnail thumbnail){
        double thumbnailMinWidth = minHeight * thumbnail.getRatio();
        if (minRunLength + totalSpacing + getSpacing() + thumbnailMinWidth <= maxRunLength || size == 0){
            size++;
            minRunLength += thumbnailMinWidth;
            totalSpacing += getSpacing();
            
            getChildren().add(index, thumbnail);
            fitToWidth();
            return true;
        }
        return false;
    }

    public synchronized boolean addNodeRecursively(Thumbnail thumbnail){
        boolean success = addNode(thumbnail);
        if (success)
            return true;

        if (nextRun == null)
            nextRun = ((ExpandedFlowPane) getParent()).addRun();
        // add node to next run, but nor recursive
        return nextRun.addNode(thumbnail);
    }


    public synchronized void pushToFirst(Thumbnail thumbnail){
        while (!addNode(0, thumbnail)){
            popLast();
        }
    }

    public synchronized void pullNext(){
        if (nextRun == null)
            return;
        boolean success = addNode(nextRun.popFirst());
        assert(success);
    }

    public synchronized Thumbnail popFirst(){
        assert(size > 0);
        Thumbnail thumbnail = (Thumbnail) getChildren().get(0);
        size--;
        minRunLength -= thumbnail.getRatio()*minHeight;
        curRunLength -= thumbnail.getRatio()*curHeight;
        totalSpacing -= getSpacing();

        //recursively pullFirst
        if (nextRun != null) {
            while (nextRun.getSize() > 0 && minRunLength + totalSpacing + getSpacing() + nextRun.getFirstMinWidth() <= maxRunLength) {
                pullNext();
            }
        }
        return thumbnail;
    }

    public synchronized void popLast(){
        assert(size > 0);
        Thumbnail thumbnail = (Thumbnail) getChildren().remove(size-1);
        size--;
        minRunLength -= thumbnail.getRatio()*minHeight;
        curRunLength -= thumbnail.getRatio()*curHeight;
        totalSpacing -= getSpacing();
        if (nextRun == null)
            nextRun = ((ExpandedFlowPane) getParent()).addRun();
        nextRun.pushToFirst(thumbnail);
    }

    public synchronized void fitToWidth(){
        double curRatio = (maxRunLength-totalSpacing) / minRunLength;
        if ((nextRun == null || nextRun.getSize() == 0) && curRatio > 2.)
            return;

        curHeight = curRatio * minHeight;

        curRunLength = 0.;
        for (Node node : getChildren()) {
            Thumbnail thumbnail = (Thumbnail) node;
            thumbnail.setFitHeight(curHeight);
            curRunLength += thumbnail.getRatio() * curHeight;
        }
    }

    public synchronized void changeMaxRunLength(double maxRunLength) {
        this.maxRunLength = maxRunLength;
        if (abs(oldRunLength-maxRunLength) > 200)
            oldRunLength = maxRunLength;
            relayout();
        fitToWidth();
    }
    
    public synchronized void relayout(){
        if (nextRun != null){
            while (nextRun.getSize() > 0 && minRunLength + totalSpacing + getSpacing() + nextRun.getFirstMinWidth() <= maxRunLength){
                pullNext();
            }
            while (minRunLength+totalSpacing > maxRunLength){
                popLast();
            }
        }
    }

    public void setNextRun(Run nextRun) {
        this.nextRun = nextRun;
    }

    public synchronized void setVgap(double value){
        setSpacing(value);
        this.totalSpacing = size*getSpacing();
        while (minRunLength+totalSpacing > maxRunLength){
            popLast();
        }
    }

    public int getSize(){
        if (nextRun != null && size == 0){
            ((ExpandedFlowPane)getParent()).removeRun(this);
            return nextRun.getSize();
        }
        return size;
    }

    public double getFirstMinWidth(){
        assert(getChildren().size() == size);
        return ((Thumbnail)getChildren().get(0)).getRatio()*minHeight;
    }
}

public class ExpandedFlowPane extends VBox {
    GlobalSettings globalSettings;
    Run lastRun;

    private double maxRunLength;
    private double minHeight;
    private double Vgap;

    public ExpandedFlowPane(){
        super();

        Vgap = 0.;
        minHeight = 300.;
//        maxRunLength = getWidth()-(getInsets().getLeft()+getInsets().getRight());
        maxRunLength = 1900;

//        globalSettings = GlobalSettings.singleton();
//        widthProperty().addListener((observable, oldValue, newValue) -> {
//            Platform.runLater(()->fitToWidth(newValue.doubleValue()-(getInsets().getLeft()+getInsets().getRight())));
//        });
    }

//    @Override
//    protected void layoutChildren() {
//        super.layoutChildren();
//        Platform.runLater(()->fitToWidth());
//    }

    public void addNode(Thumbnail thumbnail){
        assert(thumbnail.getRatio() > 0.);
        if (lastRun == null)
            addRun();
        boolean success = lastRun.addNodeRecursively(thumbnail);
        assert(success);
    }

    public Run addRun() {
        Run newRun = new Run(maxRunLength, minHeight, Vgap);
        getChildren().add(newRun);
        lastRun = newRun;

        return newRun;
    }

    public void removeRun(Run run){
        int i = getChildren().indexOf(run);
        getChildren().remove(run);
        if (getChildren().size() > i)
            ((Run)getChildren().get(i-1)).setNextRun((Run)getChildren().get(i));
    }

    public void clear(){
        getChildren().clear();
        lastRun = new Run(maxRunLength, minHeight, Vgap);
        getChildren().add(lastRun);
    }

    public synchronized void fitToWidth(double fullRunLength){
        if (fullRunLength-(getInsets().getLeft()+getInsets().getRight()) == maxRunLength)
            return;
        this.maxRunLength = fullRunLength-(getInsets().getLeft()+getInsets().getRight());

        final int len = getChildren().size();
        for (int i=0; i<len; i++){
            if (i >= getChildren().size())
                break;
            Run run = (Run)getChildren().get(i);
            run.changeMaxRunLength(maxRunLength);
        }
    }

    void setHgap(double value){
        setSpacing(value);
    }

    void setVgap(double value){
        Vgap = value;
        final int len = getChildren().size();
        for(int i=0; i<len; i++){
            Run run = (Run)getChildren().get(i);
            run.setVgap(value);
        }
    }
}
