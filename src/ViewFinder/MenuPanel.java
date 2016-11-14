package ViewFinder;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MenuPanel extends HBox {
    private static MenuPanel instance;

    private Pane galleryLabelPane;
    private Pane slideshowLabelPane;

    private Line galleryLine;
    private Line slideshowLine;


    private MenuPanel(){
        super();

        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setPadding(new Insets(15, 0, 0, 0));
        setSpacing(100);
        setAlignment(Pos.CENTER);

        Color color = Color.valueOf("#0099e5");

        Font font = Font.font("Arial", FontWeight.BOLD, 25);

        Label galleryLabel = new Label("GALLERY");
        Label slideshowLabel = new Label("SLIDESHOW");

        galleryLabel.setFont(font);
        galleryLabel.setTextFill(color);

        slideshowLabel.setFont(font);
        slideshowLabel.setTextFill(color);

        // Underline current

        galleryLine = new Line(0, 40, 100, 40);
        galleryLine.setStroke(color);
        galleryLine.setStrokeWidth(3);
        galleryLine.setVisible(false);

        slideshowLine = new Line(0, 40, 100, 40);
        slideshowLine.setStroke(color);
        slideshowLine.setStrokeWidth(3);
        slideshowLine.setVisible(false);

        galleryLabel.widthProperty().addListener((observable, oldValue, newValue)->{
            galleryLine.setEndX(newValue.doubleValue());
        });
        slideshowLabel.widthProperty().addListener((observable, oldValue, newValue)->{
            slideshowLine.setEndX(newValue.doubleValue());
        });

        galleryLabelPane = new Pane(galleryLabel, galleryLine);
        slideshowLabelPane = new Pane(slideshowLabel, slideshowLine);

        getChildren().add(galleryLabelPane);
        getChildren().add(slideshowLabelPane);
    }

    public static MenuPanel singleton(){
        if (instance == null)
            instance = new MenuPanel();
        return instance;
    }

    //clickable Labels!!
    public void setViewFinder(ViewFinder vf){
        galleryLabelPane.setOnMouseClicked(e->{
            vf.switchToGallery();
        });
        slideshowLabelPane.setOnMouseClicked(e->{
            vf.switchToSlideshow();
        });
    }

    public void setActive(String choice){
        switch(choice.toLowerCase()){
            case "gallery":
                slideshowLine.setVisible(false);
                galleryLine.setVisible(true);
                break;
            case "slideshow":
                slideshowLine.setVisible(true);
                galleryLine.setVisible(false);
                break;
        }
    }
}
