package ViewFinder;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Leo on 05.10.2016.
 */
public class ImageHandler {
    private static ImageHandler instance;

    protected Vector<File> files;
    protected HashMap<File, ImageSettings> imageSettings;
    protected HashMap<File, Image> images;
    protected HashMap<File, Thread> threads;

    private ImageHandler(){
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
        imageSettings = new HashMap<File, ImageSettings>();
        images = new HashMap<File, Image>();
        threads = new HashMap<File, Thread>();
    }

    public void addAll(File[] fileArray){
        for (File file : fileArray){
            files.add(file);
            imageSettings.put(file, new ImageSettings(file.getName(), file.getParent(), Color.gray(0.5)));
            images.put(file, null);
            threads.put(file, null);
        }
    }

    private void preload(File file){
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
        if (images.get(file) != null)
            return;

        Thread thread = new Thread(() -> {
            preload(file);
        });
        threads.put(file, thread);
        thread.start();
    }

    public void drop(int index){
        drop(files.get(index));
    }

    public void drop(File file){
        images.replace(file, null);
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
                System.out.println("Image Preloading does not work efficiently!");
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
        return imageSettings.get(file).backgroundColor;
    }

    public int getFileCount(){
        return files.size();
    }
}