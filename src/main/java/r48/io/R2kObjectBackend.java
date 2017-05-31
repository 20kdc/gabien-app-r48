/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io;

import r48.RubyIO;
import r48.io.r2k.files.MapIO;
import r48.io.r2k.files.MapTreeIO;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A beginning?
 * Created on 30/05/17.
 */
public class R2kObjectBackend implements IObjectBackend {
    public final String root;
    public R2kObjectBackend(String rootPath) {
        root = rootPath;
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        if (filename.endsWith(".lmu")) {
            try {
                FileInputStream fis = new FileInputStream(root + filename);
                RubyIO r = MapIO.readLmu(fis);
                fis.close();
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (filename.endsWith(".lmt")) {
            try {
                FileInputStream fis = new FileInputStream(root + filename);
                RubyIO r = MapTreeIO.readLmt(fis);
                fis.close();
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {

    }
}
