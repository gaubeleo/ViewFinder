package ViewFinder;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Leo on 05.10.2016.
 */
public class ImageHandler {
    private static ImageHandler instance;

    private static GlobalSettings globalSettings;

    protected Vector<File> files;
    protected HashMap<File, ImageSettings> settings;
    protected HashMap<File, Image> images;
    protected HashMap<File, Thread> threads;

    private Random random = new Random();

    private ImageHandler(){
        globalSettings = globalSettings.singleton();

        clear();
    }

    private ImageHandler(File[] fileArray){
        this();
        addAll(fileArray);
    }

    public static ImageHandler singleton(){
        if(instance == null)
            instance = new ImageHandler();

        return instance;
    }

    public static ImageHandler singleton(File[] fileArray){
        if(instance == null)
            instance = new ImageHandler(fileArray);
        else{
            instance.clear();
            instance.addAll(fileArray);
        }
        return instance;
    }

    public void clear(){
        files = new Vector<File>();
        settings = new HashMap<File, ImageSettings>();
        images = new HashMap<File, Image>();
        threads = new HashMap<File, Thread>();

        System.gc();
    }

    public void addAll(File[] fileArray){
        for (File file : fileArray){
            files.add(file);

            ImageSettings imageSettings = new ImageSettings(file.getName(), file.getParentFile().getName());
            imageSettings.load();
            settings.put(file, imageSettings);
            //Test-purpose only
            if(random.nextDouble() < 0.1){
                imageSettings.backgroundColor = Color.gray(random.nextDouble());
                imageSettings.save();
            }

            images.put(file, null);
            threads.put(file, null);
        }
    }

    public void setFiles(File[] fileArray){
        clear();
        addAll(fileArray);

        //TEMP
        preload_threaded(0);
    }

    public boolean chooseDirectory(File path){
        // will not change any files if not successful

        final File[] fileList = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });

        if (fileList == null){
            System.out.format("'%s' is not a valid path!", path);
            return false;
        }
        else if (fileList.length == 0){
            System.out.format("'%s' contains no .jpg images!", path);
            return false;
        }

        setFiles(fileList);

        return true;
    }

    public void preload(int index){
        preload(files.get(index));
    }

    public void preload(File file){
        if (images.get(file) != null && threads.get(file) != null)
            return;

        try {
            images.put(file, new Image(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
    }

    public void preload_threaded(int index){
        preload_threaded(files.get(index));
    }

    public void preload_threaded(File file){
        if (images.get(file) != null && threads.get(file) != null)
            return;

        Thread thread = new Thread(() -> {
            preload(file);
            threads.put(file, null);
        });
        threads.put(file, thread);
        thread.start();
    }

    public void drop(int index){
        drop(files.get(index));
    }

    public void drop(File file){
        images.replace(file, null);
        threads.replace(file, null);
        System.gc();
    }

    public Image get(int index){
        if (index >= getFileCount())
            return null;
        return get(files.get(index));
    }

    public Image get(File file){
        assert(files.size() == images.size());

        if (images.get(file) == null){
            if (threads.get(file) == null){
                System.out.println("Image Preloading does not work correctly!");
                preload(file);
                return images.get(file);
            }
            try {
                // wait for preloading to finish
                threads.get(file).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return images.get(file);
    }

    public Color getBackgroundColor(int index){
        return getBackgroundColor(files.get(index));
    }

    public Color getBackgroundColor(File file){
        ImageSettings imageSettings = settings.get(file);
        if (settings.get(file).backgroundColor != null)
            return imageSettings.backgroundColor;
        return globalSettings.backgroundColor;
    }

    public int getFileCount(){
        return files.size();
    }
}