package ViewFinder;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class KeyController {
    private static KeyController instance;

    private ViewFinder vf;
    private StartScreen startScreen;
    private Slideshow slideshow;
    private Gallery gallery;

    private KeyController(ViewFinder vf) {
        this.vf = vf;
    }

    public static KeyController singleton(ViewFinder vf){
        if (instance == null)
            instance = new KeyController(vf);
        return instance;
    }

    private void handleGlobal(KeyEvent e){
        switch (e.getCode()) {
            case F5:
                vf.toggleFullscreen();
                break;

            case N:
                if (e.isControlDown())
                    vf.newProject();
                break;

            case O:
                if (e.isControlDown())
                    vf.openProject();
                break;
        }
    }

    public void handleStartScreen(KeyEvent e){
        switch (e.getCode()) {
            case I:
                startScreen.slidePanels();
                break;
        }

        handleGlobal(e);
    }

    public void handleGallery(KeyEvent e){
        switch (e.getCode()) {
            case ESCAPE:
                vf.switchToSlideshow(gallery.getIndex());
                break;
            case UP:
                gallery.up();
                //gallery.scrollUp();
                break;
            case DOWN:
                gallery.down();
                //gallery.scrollDown();
                break;
            case RIGHT:
                gallery.next();
                break;
            case LEFT:
                gallery.previous();
                break;
            case ENTER:
                vf.switchToSlideshow(gallery.getIndex());
                break;
            case I:
                gallery.slidePanels();
                break;
        }

        handleGlobal(e);
    }

    public void handleSlideshow(KeyEvent e){
        switch (e.getCode()) {
            case ESCAPE:
                vf.switchToGallery(slideshow.getIndex());
                break;
            case LEFT:
                slideshow.previous();
                break;
            case RIGHT:
                slideshow.next();
                break;
            case UP:
                slideshow.brightenBackground();
                break;
            case DOWN:
                slideshow.darkenBackground();
                break;

            case SPACE:
                slideshow.zoom();
                break;

            case I:
                slideshow.slidePanels();
                break;
            case F:
                slideshow.frameImage();
                break;
        }

        handleGlobal(e);
    }

    public void setStartScreen(StartScreen startScreen) {
        this.startScreen = startScreen;
    }

    public void setSlideshow(Slideshow slideshow) {
        this.slideshow = slideshow;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

}