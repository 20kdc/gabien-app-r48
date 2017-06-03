/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io;

import r48.RubyIO;
import r48.io.r2k.files.DatabaseIO;
import r48.io.r2k.files.MapIO;
import r48.io.r2k.files.MapTreeIO;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        if (filename.endsWith(".ldb")) {
            try {
                FileInputStream fis = new FileInputStream(root + filename);
                RubyIO r = DatabaseIO.readLdb(fis);
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Note the write occurs before the F.O.S is created for safety
        if (filename.endsWith(".lmu")) {
            MapIO.writeLmu(baos, object);
            FileOutputStream fos = new FileOutputStream(root + filename);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".lmt")) {
            MapTreeIO.writeLmt(baos, object);
            FileOutputStream fos = new FileOutputStream(root + filename);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".ldb")) {
            DatabaseIO.writeLdb(baos, object);
            FileOutputStream fos = new FileOutputStream(root + filename);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        throw new IOException("Unknown how to save " + filename + " (lmu/lmt/ldb)");
    }
}
