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

public class ImageHandler {
    private static ImageHandler instance;

    private static GlobalSettings globalSettings;

    protected Vector<File> files;
    protected HashMap<File, ImageSettings> settings;
    protected HashMap<File, Image> images;
    protected HashMap<File, Image> thumbnails;
    protected HashMap<File, Thread> threads;
    protected HashMap<File, Thread> thumbnailThreads;

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
        thumbnails = new HashMap<File, Image>();
        threads = new HashMap<File, Thread>();
        thumbnailThreads = new HashMap<File, Thread>();

        System.gc();
    }

    public void addAll(File[] fileArray){
        for (File file : fileArray){
            files.add(file);

            ImageSettings imageSettings = new ImageSettings(file.getName(), globalSettings.projectName);
            imageSettings.load();
            settings.put(file, imageSettings);
            //Test-purpose only
            if(random.nextDouble() < 0.1){
                imageSettings.backgroundColor = Color.gray(random.nextDouble());
                imageSettings.save();
            }

            images.put(file, null);
            thumbnails.put(file, null);
            threads.put(file, null);
            thumbnailThreads.put(file, null);
        }
        //preloadThumbnailsThreaded();
    }

    public void setFiles(File[] fileArray){
        clear();
        addAll(fileArray);

        //TEMP
        preloadThreaded(0);
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

    public void preloadThumbnailsThreaded(){
        for (int i=0; i<4; i++){
            Thread thread = new Thread(() -> {
                for (File file : files){
                    preloadThumbnailThreaded(file);

                    try {
                        thumbnailThreads.get(file).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    public void preloadThumbnailThreaded(File file){
        if (thumbnails.get(file) != null || thumbnailThreads.get(file) != null)
            return;

        Thread thread = new Thread(() -> {
            preloadThumbnail(file);
            thumbnailThreads.put(file, null);
        });
        thumbnailThreads.put(file, thread);
        thread.start();
    }

    public void preloadThumbnail(File file){
        if (thumbnails.get(file) != null)
            return;
        try {
            thumbnails.put(file, new Image(new FileInputStream(file), 0, 400, true, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
    }

    public void preload(int index){
        preload(files.get(index));
    }

    public void preload(File file){
        if (images.get(file) != null)
            return;

        try {
            images.put(file, new Image(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
    }

    public void preloadThreaded(int index){
        preloadThreaded(files.get(index));
    }

    public void preloadThreaded(File file){
        if (images.get(file) != null || threads.get(file) != null)
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

    public Image getThumbnail(int index) {
        if (index >= getFileCount())
            return null;
        return getThumbnail(files.get(index));
    }

    private Image getThumbnail(File file) {
        assert(files.size() == thumbnails.size());

        if (thumbnails.get(file) == null){
            if (thumbnailThreads.get(file) == null){
                System.out.println("Image-Thumbnail Preloading does not work correctly!");
                preloadThumbnail(file);
                return thumbnails.get(file);
            }
            try {
                // wait for preloading to finish
                thumbnailThreads.get(file).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return thumbnails.get(file);
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