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

public class Menu  extends HBox {
    private static Menu instance;

    private Line galleryLine;
    private Line slideshowLine;

    Line line;

    private Menu(){
        super();
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setPadding(new Insets(20, 100, 0, 100));
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

        galleryLabel.widthProperty().addListener((observable, oldValue, newValue) ->{
            galleryLine.setEndX(newValue.doubleValue());
        });
        slideshowLabel.widthProperty().addListener((observable, oldValue, newValue) ->{
            slideshowLine.setEndX(newValue.doubleValue());
        });

        Pane galleryLabelPane = new Pane(galleryLabel, galleryLine);
        Pane slideshowLabelPane = new Pane(slideshowLabel, slideshowLine);

        getChildren().add(galleryLabelPane);
        getChildren().add(slideshowLabelPane);

        line = new Line();

        getChildren().add(line);
    }

    public static Menu singleton(){
        if (instance == null)
            instance = new Menu();
        return instance;
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
