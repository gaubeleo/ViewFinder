package ViewFinder;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class ExpandedFlowPane extends FlowPane {
    GlobalSettings globalSettings;

    public ExpandedFlowPane(){
        super();

//        globalSettings = GlobalSettings.singleton();
//        .addListener((observable, oldValue, newValue) -> {
//            System.out.println(oldValue);
//
//            expandToWidth();
//        });
    }

    @Override
    protected void layoutChildren() {
        expandToWidth();
        super.layoutChildren();
    }

    public void clear(){
        getChildren().clear();
    }

    public Thumbnail getChild(int i){
        return (Thumbnail) getChildren().get(i);
    }

    public synchronized void expandToWidth(){
        // currently only works for Horizontal FlowPanes
        assert(getOrientation() == Orientation.HORIZONTAL);
        assert(getWidth() > 0);

        double maxRunLength = getWidth()-(getInsets().getLeft()+getInsets().getRight());
        double currentRunLength = 0.;
        int firstRowIndex = 0;

        final List<Node> children = getChildren();

        //automatically ignores last row!
        int i;
        for (i=0; i<children.size(); i++) {
            Image img = ((ImageView)children.get(i)).getImage();
            if (img == null)
                continue;
            //double ratio = img.getWidth()/img.getHeight();
            double childWidth = img.getWidth();

            if (currentRunLength + childWidth + (i-firstRowIndex)*getHgap() <= maxRunLength)
                currentRunLength += childWidth;
            else {
                double newRatio = currentRunLength/(maxRunLength-(getHgap()*((i-1)-firstRowIndex)));
                double newHeight = 300./newRatio;

                double total =0.;
                for (int j=firstRowIndex; j<i; j++){
                    Thumbnail thumbnail = (Thumbnail) children.get(j);
                    thumbnail.setFitHeight(newHeight);
                    total += thumbnail.getBoundsInParent().getWidth();
                }
                ImageView ivv = (ImageView) children.get(i-1);
                ivv.setFitHeight(newHeight-3);
                total =0.;

                for (int j=firstRowIndex; j<i; j++){
                    ImageView iv = (ImageView) children.get(j);
                    total+= iv.getBoundsInParent().getWidth();
                }
                //System.out.println(maxRunLength - (total+getHgap()*((i-1)-firstRowIndex)));
                
                currentRunLength = childWidth;
                firstRowIndex = i;
            }
        }
/*        double ratio = currentRunLength/(maxRunLength-(getHgap()*((i-1)-firstRowIndex)));
        double newHeight = 300/ratio;

        for (int j=firstRowIndex; j<i; j++){
            ImageView iv = (ImageView) children.get(j);
            iv.setFitHeight(newHeight);
        }*/
    }
}
