package Tests;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;


public class MetadataTest {
    public static void main(String[] args) {
        try {
            new MetadataTest().changeXMPRating(new File(".\\src\\data\\test.JPG"), new File(".\\src\\data\\test2.JPG"), '4');
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This example illustrates how to remove a tag (if present) from EXIF
     * metadata in a JPEG file.
     *
     * In this case, we remove the "aperture" tag from the EXIF metadata if
     * present.
     *
     * @param src
     *            A source image file.
     * @param dst
     *            The output file.
     * @throws IOException
     * @throws ImageReadException
     * @throws ImageWriteException
     */
    public void changeXMPRating(final File src, final File dst, final char rating) throws IOException, ImageReadException, ImageWriteException {
        try (FileOutputStream fos = new FileOutputStream(dst); OutputStream os = new BufferedOutputStream(fos)) {
            final String ratingTag = "xmp:Rating";

            final String xmp = new JpegImageParser().getXmpXml(new ByteSourceFile(src), null);

            StringBuilder newXMP = new StringBuilder(xmp);
            newXMP.setCharAt(xmp.indexOf(ratingTag)+ratingTag.length()+2, rating);

            new JpegXmpRewriter().updateXmpXml(src, os, newXMP.toString());

            System.out.format("new rating for '%s': %c", dst, rating);
        }
    }
}
