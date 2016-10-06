package ViewFinder;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Created by Leo on 05.10.2016.
 */
public class ImageHandler {
    protected HashMap<File, Image> images;
    protected HashMap<File, Thread> threads;

    public ImageHandler(File[] files){
        images = new HashMap<File, Image>();
        threads = new HashMap<File, Thread>();

        for (File file : files){
            images.put(file, null);
            threads.put(file, null);
        }
    }

    public void preload(File file){
        try {
            images.put(file, new Image(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
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

    public void drop(File file){
        images.replace(file, null);

        System.gc();
    }

    public Image get(File file){
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
}