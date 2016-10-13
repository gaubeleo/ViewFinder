package ViewFinder;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class FileChooser extends Stage {

    public FileChooser() {

    }

    public File chooseImageFolder(){
        return chooseImageFolder(new File(System.getProperty("user.home"), "Pictures"));
    }

    public File chooseImageFolder(File defaultDirectory){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("JavaFX Projects");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(this);

        return selectedDirectory;
    }

    public String chooseProjectName(String defaultName){
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("New Project");
        dialog.setHeaderText("Please choose a Name for your new Project:");

        Optional<String> result = dialog.showAndWait();
        String input = "";
        if (result.isPresent()) {
            input = result.get();
        }
        return input;
    }

    public void allertNoImages(){
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Warning");
        dialog.setHeaderText(null);
        dialog.setContentText("This Directory does not contain any images!");

        dialog.showAndWait();
    }
}
