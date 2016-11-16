package ViewFinder;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static java.lang.Math.abs;
import static java.lang.Math.max;

class Run extends HBox {
    private int size;
    private int frameOffset;
    private int totalSpacing;
    private int minRunLength;
    private int curRunLength;
    private int oldRunLength;
    private int maxRunLength;
    private int minHeight;
    private int curHeight;
    private Run nextRun;


    public Run(int maxRunLength, int minHeight, int Vgap){
        super();

        this.size = 0;
        this.totalSpacing = 0;
        this.minRunLength = 0;
        this.curRunLength = 0;
        this.oldRunLength = 0;
        this.maxRunLength = maxRunLength;
        this.minHeight = minHeight;

        setSpacing(Vgap);
    }

    public synchronized boolean addNode(Thumbnail thumbnail){
        return addNode(size, thumbnail);
    }

    public synchronized boolean addNode(int index, Thumbnail thumbnail){
        frameOffset = thumbnail.getFrameSize()*2;
        int thumbnailMinWidth = (int)(minHeight * thumbnail.getRatio());
        if (minRunLength + totalSpacing + getSpacing() + frameOffset + thumbnailMinWidth <= maxRunLength || size == 0){
            size++;
            minRunLength += thumbnailMinWidth;
            totalSpacing += getSpacing() + frameOffset;
            
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
        minRunLength -= (int)(thumbnail.getRatio()*minHeight);
        curRunLength -= (int)(thumbnail.getRatio()*curHeight);
        totalSpacing -= getSpacing() + frameOffset;

        //recursively pullFirst
        if (nextRun != null) {
            while (nextRun.getSize() > 0 && minRunLength + totalSpacing + getSpacing() + frameOffset + nextRun.getFirstMinWidth() <= maxRunLength) {
                pullNext();
            }
        }
        return thumbnail;
    }

    public synchronized void popLast(){
        assert(size > 0);
        Thumbnail thumbnail = (Thumbnail) getChildren().remove(size-1);
        size--;
        minRunLength -= (int)(thumbnail.getRatio()*minHeight);
        curRunLength -= (int)(thumbnail.getRatio()*curHeight);
        totalSpacing -= getSpacing() + frameOffset;
        if (nextRun == null)
            nextRun = ((ExpandedFlowPane) getParent()).addRun();
        nextRun.pushToFirst(thumbnail);
    }

    public synchronized void fitToWidth(){
        double curRatio = (double)(maxRunLength-totalSpacing) / (double) minRunLength;
        if ((nextRun == null || nextRun.getSize() == 0) && curRatio > 2.)
            return;

        double newHeight = curRatio * minHeight;
        curHeight = (int)newHeight;

        curRunLength = 0;
        for (Node node : getChildren()) {
            Thumbnail thumbnail = (Thumbnail) node;
            int width = (int)(thumbnail.getRatio() * newHeight + 0.5);
            thumbnail.setFitSize(curHeight, width);
            curRunLength += width;
        }
        // still a few pixels off!
    }

    public synchronized void changeMaxRunLength(int maxRunLength) {
        this.maxRunLength = maxRunLength;
        if (abs(oldRunLength-maxRunLength) > 300)
            oldRunLength = maxRunLength;
            relayout();
        fitToWidth();
    }
    
    public synchronized void relayout(){
        if (nextRun != null){
            while (nextRun.getSize() > 0 && minRunLength + totalSpacing + getSpacing() + frameOffset + nextRun.getFirstMinWidth() <= maxRunLength){
                pullNext();
            }
            while (minRunLength + totalSpacing > maxRunLength){
                popLast();
            }
        }
    }

    public void setNextRun(Run nextRun) {
        this.nextRun = nextRun;
    }

    public synchronized void setVgap(double value){
        setSpacing(value);
        this.totalSpacing = (int)(size*(getSpacing()+frameOffset));
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

    public int getFirstMinWidth(){
        assert(getChildren().size() == size);
        return (int)(((Thumbnail)getChildren().get(0)).getRatio()*minHeight);
    }
}

public class ExpandedFlowPane extends VBox {
    GlobalSettings globalSettings;
    Run lastRun;

    private int maxRunLength;
    private int minHeight;
    private int Vgap;

    public ExpandedFlowPane(){
        super();

        Vgap = 0;
        minHeight = 300;
//        maxRunLength = getWidth()-(getInsets().getLeft()+getInsets().getRight());
        maxRunLength = 1900;

//        globalSettings = GlobalSettings.singleton();
//        widthProperty().addListener((observable, oldValue, newValue) -> {
//            Platform.runLater(()->fitToWidth(newValue.intValue()-(getInsets().getLeft()+getInsets().getRight())));
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

    public synchronized void fitToWidth(int fullRunLength){
        int oldRunLength = this.maxRunLength;
        this.maxRunLength = (int)(fullRunLength-(getInsets().getLeft()+getInsets().getRight()));
        if (oldRunLength == maxRunLength)
            return;

        final int len = getChildren().size();
        for (int i=0; i<len; i++){
            if (i >= getChildren().size())
                break;
            Run run = (Run)getChildren().get(i);
            run.changeMaxRunLength(maxRunLength);
        }
    }

    void setHgap(int value){
        setSpacing(value);
    }

    void setVgap(int value){
        Vgap = value;
        final int len = getChildren().size();
        for(int i=0; i<len; i++){
            Run run = (Run)getChildren().get(i);
            run.setVgap(value);
        }
    }
}
