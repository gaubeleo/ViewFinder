package ViewFinder;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class ExpandedFlowPane extends FlowPane {

    public ExpandedFlowPane(ScrollPane scrollPane){
        super();

        widthProperty().addListener((observable, oldValue, newValue) -> {
            expandToWidth();
        });
    }

    public void expandToWidth(){
        // currently only works for Horizontal FlowPanes
        assert(getOrientation() == Orientation.HORIZONTAL);
        assert(getWidth() > 0);

        double maxRunLength = getWidth()-(getInsets().getLeft()+getInsets().getRight()) - 3;
        double currentRunLength = 0.;
        int firstRowIndex = 0;

        final List<Node> children = getChildren();

        //automatically ignores last row!
        int i;
        for (i=0; i<children.size(); i++) {
            ImageView child = (ImageView) children.get(i);
            child.setFitHeight(300);
            if (child.isManaged()) {
                double childWidth = child.getLayoutBounds().getWidth();
                if (currentRunLength + childWidth + (i-firstRowIndex)*getHgap() <= maxRunLength)
                    currentRunLength += childWidth;
                else {
                    double ratio = currentRunLength/(maxRunLength-(getHgap()*((i-1)-firstRowIndex)));
                    double newHeight = 300/ratio;

                    for (int j=firstRowIndex; j<i; j++){
                        ImageView iv = (ImageView) children.get(j);
                        iv.setFitHeight(newHeight);
                    }
                    currentRunLength = childWidth;
                    firstRowIndex = i;
                }
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
