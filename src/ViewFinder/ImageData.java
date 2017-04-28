package ViewFinder;

import java.io.File;
import java.util.Date;
import java.util.Vector;

class ImageData {
    public String filename;
    public String directory;
    public String path;

    public String iso;
    public String aperture;
    public String shutter;

    public Date dateTaken;

    public Vector<String> keywords;

    public ZoomPos startZoom;
    public ZoomPos endZoom;

    public ImageData(File filename){

    }
}
