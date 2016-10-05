package ViewFinder;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

public class KeyController {

    ViewFinder vf;

    public KeyController(ViewFinder vf) {
        this.vf = vf;
    }

    public void manageSlideshow(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent key) {
                synchronized (KeyController.class) {
                    switch (key.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            switch (key.getKeyCode()) {
                                case KeyEvent.VK_RIGHT:
                                    vf.next();
                                case KeyEvent.VK_LEFT:
                                    vf.previous();
                                case KeyEvent.VK_I:
                                    vf.slideInOut();
                            }
                            break;

                        case KeyEvent.KEY_RELEASED:
                            if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
                            }
                            break;
                    }
                    return false;
                }
            }
        });
    }
    }