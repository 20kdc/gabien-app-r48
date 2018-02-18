/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Fake GaBIEn class for use by r48.io
 * Created on 1st December 2017.
 */
public class GaBIEn {
    public static InputStream getResource(String s) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(s);
    }

    public static BufferedImage getImage(String s) {
        try {
            return ImageIO.read(getResource(s));
        } catch (Exception e) {
            e.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public static InputStream getInFile(String f) {
        try {
            return new FileInputStream(f);
        } catch (Exception e) {
            return null;
        }
    }

    public static OutputStream getOutFile(String f) {
        try {
            return new FileOutputStream(f);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean fileOrDirExists(String s) {
        return new File(s).exists();
    }
    public static boolean dirExists(String s) {
        return new File(s).isDirectory();
    }

    public static String[] listEntries(String s) {
        return new File(s).list();
    }

    public static String basename(String s) {
        int p = s.lastIndexOf('/');
        if (p == -1)
            return s;
        return s.substring(p + 1);
    }

    public static void makeDirectories(String s) {
        new File(s).mkdirs();
    }
}
