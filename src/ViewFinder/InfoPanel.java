package ViewFinder;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class InfoPanel extends Slideout{
    private static InfoPanel instance;

    private InfoPanel(){
        super(300, Pos.BASELINE_RIGHT, new Label("Info"));
        setStyle("-fx-background-color: rgb(100, 100, 100);");
        setPadding(new Insets(25, 25, 25, 25));
    }

    public static InfoPanel singleton(){
        if (instance == null)
            instance = new InfoPanel();
        return instance;
    }
}
