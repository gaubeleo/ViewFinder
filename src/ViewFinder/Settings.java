package ViewFinder;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.*;

abstract class Settings {
    protected File file;

    protected String fileName;
    protected String projectName;
    protected final String extension = ".set";

    public Settings(String fileName, String projectName){
        changeFilePath(fileName, projectName);
    }

    protected void changeFilePath(String fileName, String projectName){
        this.fileName = fileName.replace(extension, "")+extension;
        this.projectName = projectName;

        file = new File(".\\data\\projects\\"+this.projectName+"\\"+this.fileName);
    }

    public abstract boolean load();

    public abstract boolean save();
}

class ImageSettings extends Settings {
    public Color backgroundColor;
    public boolean hasFrame;
    public Color frameColor;
    public int frameSize;



    public ImageSettings(String fileName, String projectName){
        super(fileName, projectName);
    }

    @Override
    public boolean save(){
        File dir = file.getParentFile();
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);

            writeImageSettings(fileWriter);

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void writeImageSettings(PrintWriter fileWriter){
        //handle "null"-Exceptions for primitive types


        if (backgroundColor != null)
            fileWriter.println("backgroundColor -> "+backgroundColor); // format: 0xffffffff
        if (frameColor!= null)
            fileWriter.println("frameColor -> "+frameColor); // format: 0xffffffff
        fileWriter.println("frameSize -> "+String.valueOf(frameSize));
        fileWriter.println("hasFrame -> "+String.valueOf(hasFrame));
    }

    @Override
    public boolean load(){
        if (!file.isFile())
            return false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line, id, value;
            String[] splitLine;
            while ((line = reader.readLine()) != null && line != "\n") {
                assert(line.contains(" -> "));
                splitLine = line.split(" -> ", 2);
                id = splitLine[0].trim();
                value = splitLine[1].trim();

                handleImageId(id, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean handleImageId(String id, String value){
        try{
            switch(id){
                case "backgroundColor":
                    backgroundColor = Color.valueOf(value);
                    break;
                case "frameColor":
                    frameColor = Color.valueOf(value);
                    break;
                case "frameSize":
                    frameSize = Integer.valueOf(value);
                    break;
                case "hasFrame":
                    hasFrame = Boolean.valueOf(value);
                    break;
                default:
                    System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
                    return false;
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        return true;
    }
}

class GlobalSettings extends ImageSettings{
    private static GlobalSettings instance;
    private static GlobalSettings onStartSettings;

    public boolean fullscreen;

    public File mostRecentProject;
    public File imagePath;
    public int preloadCount;
    public String onStartAction;

    public Color backgroundColor;
    public Color panelColor;

    public Duration fadeDuration;
    private GlobalSettings(String projectName) {
        super("OnStartSettings", projectName);

        fullscreen = false;

        mostRecentProject = new File(".");
        imagePath = new File("");
        onStartAction = "Default";
        preloadCount = 1;

        //set ImageSettings
        backgroundColor = Color.gray(0.25);
        frameColor = Color.valueOf("#0099e5");
        frameSize = 4;
        hasFrame = true;

        panelColor = Color.gray(0.25);
        fadeDuration = new Duration(500);
    }

    public static GlobalSettings singleton(){
        if (instance == null){
            instance = new GlobalSettings(".");
            onStartSettings = new GlobalSettings(".");
        }

        return instance;
    }


    public static void updateMostRecentProject(File projectPath){
        onStartSettings.mostRecentProject = projectPath;
        onStartSettings.save();
    }

    @Override
    public boolean save() {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(file);

            writeSettings(fileWriter);
            writeImageSettings(fileWriter);

            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void writeSettings(PrintWriter fileWriter){
        //handle "null"-Exceptions for primitive types
        if (fileName.compareTo("OnStartSettings.set") == 0)
            fileWriter.println("mostRecentProject -> "+mostRecentProject.getAbsolutePath());
        if (imagePath != null)
            fileWriter.println("imagePath -> "+imagePath.getAbsolutePath());
        if (fadeDuration != null)
            fileWriter.println("fadeDuration -> "+String.valueOf((int)fadeDuration.toMillis()));
        fileWriter.println("preloadCount -> "+String.valueOf(preloadCount));
        if (backgroundColor != null)
            fileWriter.println("backgroundColor -> "+backgroundColor.toString()); // format: 0xffffffff
        if (panelColor != null)
            fileWriter.println("panelColor -> "+panelColor.toString()); // format: 0xffffffff
    }

    @Override
    public boolean load(){
        if (!file.isFile())
            return false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line, id, value;
            String[] splitLine;
            while ((line = reader.readLine()) != null && line != "\n") {
                assert(line.contains(" -> "));
                splitLine = line.split(" -> ", 2);
                id = splitLine[0].trim();
                value = splitLine[1].trim();

                if (!handleId(id, value))
                    handleImageId(id, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean handleId(String id, String value){
        try{
            switch(id){
                case "mostRecentProject":
                   mostRecentProject = new File(value);
                case "imagePath":
                    imagePath = new File(value);
                    break;
                case "fadeDuration":
                    fadeDuration = new Duration(Integer.valueOf(value));
                    break;
                case "preloadCount":
                    preloadCount = Integer.valueOf(value);
                    break;
                case "backgroundColor":
                    backgroundColor = Color.valueOf(value);
                    break;
                case "panelColor":
                    panelColor = Color.valueOf(value);
                    break;
                case "onStartAction":
                    onStartAction = value;
                    break;
                default:
                    //System.out.format("WARNING encountered unknown id '%s' and value '%s' while reading file '%s'\n", id, value, file.toString());
                    return false;
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        return true;
    }


    public void newProject(String projectName, File imagePath){
        this.imagePath = imagePath;

        changeFilePath("GlobalSettings.set", projectName);
        save();
    }

    public boolean openProject(String openProjectName){
        changeFilePath("GlobalSettings.set", openProjectName);
        if (!file.exists()){
            System.out.println("GlobalSettings missing: ");
            System.out.println(file.getAbsolutePath());
            return false;
        }
        return load();
    }
}
