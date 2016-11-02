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
        chooser.setTitle("Create new Project");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(this);

        return selectedDirectory;
    }
    public File chooseExistingProject() {return chooseExistingProject(new File(".\\data\\projects\\"));}

    public File chooseExistingProject(File defaultDirectory) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open existing Project");
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

    public void alertNoImages(){
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Warning");
        dialog.setHeaderText(null);
        dialog.setContentText("This Directory does not contain any images!");

        dialog.showAndWait();
    }

    public void alertInvalidProjectName() {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Warning");
        dialog.setHeaderText(null);
        dialog.setContentText("This is not a valid name for a ViewFinder-Project!");

        dialog.showAndWait();
    }

    public void alertNoProject() {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Warning");
        dialog.setHeaderText("You selected a directory that is no ViewFinder Project!");
        dialog.setContentText("All ViewFinder-Projects contain a File called 'GlobalSettings.set'!");

        dialog.showAndWait();
    }

    public void alertMissingImages() {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Warning");
        dialog.setHeaderText("This Project contains images which could not be found!");
        dialog.setContentText("Would you like to specify the new location of ... (needs to be implemented)!");

        dialog.showAndWait();
    }
}
