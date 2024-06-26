/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.uslx.vfs.FSBackend;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.r2k.R2kIO;
import r48.io.r2k.obj.MapUnit;
import r48.io.r2k.obj.Save;
import r48.io.r2k.obj.ldb.Database;
import r48.io.r2k.struct.MapTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A beginning?
 * Created on 30/05/17.
 */
public class R2kObjectBackend extends OldObjectBackend<RORIO, IRIO> {
    public final String root;
    public final FSBackend fs;

    public R2kObjectBackend(FSBackend fs, String rootPath) {
        this.fs = fs;
        root = rootPath;
    }

    @Override
    public IRIO newObjectO(String fn, @NonNull DMContext context) {
        // Non-RubyIO things
        if (fn.endsWith(".lmt"))
            return new MapTree(context);
        if (fn.endsWith(".lmu"))
            return new MapUnit(context);
        if (fn.endsWith(".ldb"))
            return new Database(context);
        if (fn.endsWith(".lsd"))
            return new Save(context);
        return new IRIOGeneric(context);
    }

    @Override
    public IRIO loadObjectFromFile(String filename, @NonNull DMContext context) {
        filename = root + filename;
        try (InputStream fis = fs.intoPath(filename).openRead()) {
            try {
                if (filename.endsWith(".lmu")) {
                    return R2kIO.readLmu(context, fis);
                } else if (filename.endsWith(".lmt")) {
                    return R2kIO.readLmt(context, fis);
                } else if (filename.endsWith(".ldb")) {
                    return R2kIO.readLdb(context, fis);
                } else if (filename.endsWith(".lsd")) {
                    return R2kIO.readLsd(context, fis);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception ex) {
            // failed to open, so doesn't exist
        }
        return null;
    }

    @Override
    public void saveObjectToFile(String filename, RORIO object) throws IOException {
        filename = root + filename;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Note the write occurs before the F.O.S is created for safety
        if (filename.endsWith(".lmu")) {
            R2kIO.writeLmu(baos, (MapUnit) object);
        } else if (filename.endsWith(".lmt")) {
            R2kIO.writeLmt(baos, (MapTree) object);
        } else if (filename.endsWith(".ldb")) {
            R2kIO.writeLdb(baos, (Database) object);
        } else if (filename.endsWith(".lsd")) {
            R2kIO.writeLsd(baos, (Save) object);
        } else {
            throw new IOException("Unknown how to save " + filename + " (lmu/lmt/ldb)");
        }
        try (OutputStream fos = fs.intoPath(filename).openWrite()) {
            baos.writeTo(fos);
        }
    }
}
