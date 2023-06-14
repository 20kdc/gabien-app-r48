/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.uslx.vfs.FSBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.data.obj.DM2Context;
import r48.io.r2k.R2kIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.MapUnit;
import r48.io.r2k.obj.Save;
import r48.io.r2k.obj.ldb.Database;
import r48.io.r2k.struct.MapTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A beginning?
 * Created on 30/05/17.
 */
public class R2kObjectBackend extends OldObjectBackend<RORIO, IRIO> {
    public final String root;
    public final Charset charset;
    public final DM2Context dm2c;
    public final FSBackend fs;

    public R2kObjectBackend(FSBackend fs, String rootPath, Charset cs) {
        this.fs = fs;
        root = rootPath;
        charset = cs;
        dm2c = new DM2Context(cs);
    }

    @Override
    public IRIO newObjectO(String fn) {
        // Non-RubyIO things
        if (fn.endsWith(".lmt"))
            return new MapTree(dm2c);
        if (fn.endsWith(".lmu"))
            return new MapUnit(dm2c);
        if (fn.endsWith(".ldb"))
            return new Database(dm2c);
        if (fn.endsWith(".lsd"))
            return new Save(dm2c);
        return new IRIOGeneric(charset);
    }

    @Override
    public IRIO loadObjectFromFile(String filename) {
        filename = root + filename;
        String str = PathUtils.autoDetectWindows(fs, filename);
        try (InputStream fis = fs.openRead(str)) {
            if (filename.endsWith(".lmu")) {
                try {
                    MapUnit r = R2kIO.readLmu(dm2c, fis);
                    fis.close();
                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (filename.endsWith(".lmt")) {
                try {
                    IRIO r = R2kIO.readLmt(dm2c, fis);
                    fis.close();
                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (filename.endsWith(".ldb")) {
                try {
                    Database r = R2kIO.readLdb(dm2c, fis);
                    fis.close();
                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (filename.endsWith(".lsd")) {
                try {
                    Save r = R2kIO.readLsd(dm2c, fis);
                    fis.close();
                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (Exception ex) {
            // failed to open, so doesn't exist
        }
        return null;
    }

    @Override
    public void saveObjectToFile(String filename, RORIO object) throws IOException {
        filename = root + filename;
        String str = PathUtils.autoDetectWindows(fs, filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Note the write occurs before the F.O.S is created for safety
        if (filename.endsWith(".lmu")) {
            R2kIO.writeLmu(baos, (MapUnit) object);
            OutputStream fos = fs.openWrite(str);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".lmt")) {
            R2kIO.writeLmt(baos, (MapTree) object);
            OutputStream fos = fs.openWrite(str);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".ldb")) {
            R2kIO.writeLdb(baos, (Database) object);
            OutputStream fos = fs.openWrite(str);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".lsd")) {
            R2kIO.writeLsd(baos, (Save) object);
            OutputStream fos = fs.openWrite(str);
            baos.writeTo(fos);
            fos.close();
            return;
        }
        throw new IOException("Unknown how to save " + filename + " (lmu/lmt/ldb)");
    }

    @Override
    public String userspaceBindersPrefix() {
        return R2kUtil.userspaceBinder;
    }
}
