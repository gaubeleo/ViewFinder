package ViewFinder;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class SettingsPanel extends Slideout{
    private static SettingsPanel instance;

    private SettingsPanel(){
        super(300, Pos.BASELINE_LEFT, new Label("Settings"));
        setStyle("-fx-background-color: rgb(255, 255, 255);");
        setPadding(new Insets(25, 25, 25, 25));
    }

    public static SettingsPanel singleton(){
        if (instance == null)
            instance = new SettingsPanel();
        return instance;
    }
}
