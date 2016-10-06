package ViewFinder;

import javafx.scene.input.KeyEvent;

public class KeyController {
    private static KeyController instance;
    private ViewFinder vf;

    private KeyController(ViewFinder vf) {
        this.vf = vf;
    }

    public static KeyController singleton(ViewFinder vf){
        if (instance == null)
            instance = new KeyController(vf);
        return instance;
    }

    public void manageSlideshow(KeyEvent event) {
        switch (event.getCode()) {
            case UP:    break;
            case DOWN:  break;
            case LEFT:  vf.previous(); break;
            case RIGHT: vf.next(); break;
            case I: vf.slideInOut(); break;
        }
    }
}