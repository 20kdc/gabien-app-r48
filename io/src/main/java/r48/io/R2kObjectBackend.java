/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.GaBIEn;
import r48.io.data.DM2Context;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.files.DatabaseIO;
import r48.io.r2k.files.MapIO;
import r48.io.r2k.files.MapTreeIO;
import r48.io.r2k.files.SaveDataIO;
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

    public R2kObjectBackend(String rootPath, Charset cs) {
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
        String str = PathUtils.autoDetectWindows(filename);
        if (filename.endsWith(".lmu")) {
            try {
                InputStream fis = GaBIEn.getInFile(str);
                if (fis == null)
                    return null;
                MapUnit r = MapIO.readLmu(dm2c, fis);
                fis.close();
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (filename.endsWith(".lmt")) {
            try {
                InputStream fis = GaBIEn.getInFile(str);
                if (fis == null)
                    return null;
                IRIO r = MapTreeIO.readLmt(dm2c, fis);
                fis.close();
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (filename.endsWith(".ldb")) {
            try {
                InputStream fis = GaBIEn.getInFile(str);
                if (fis == null)
                    return null;
                Database r = DatabaseIO.readLdb(dm2c, fis);
                fis.close();
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (filename.endsWith(".lsd")) {
            try {
                InputStream fis = GaBIEn.getInFile(str);
                if (fis == null)
                    return null;
                Save r = SaveDataIO.readLsd(dm2c, fis);
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
    public void saveObjectToFile(String filename, RORIO object) throws IOException {
        filename = root + filename;
        String str = PathUtils.autoDetectWindows(filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Note the write occurs before the F.O.S is created for safety
        if (filename.endsWith(".lmu")) {
            MapIO.writeLmu(baos, (MapUnit) object);
            OutputStream fos = GaBIEn.getOutFile(str);
            if (fos == null)
                throw new IOException("Unable to open a file.");
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".lmt")) {
            MapTreeIO.writeLmt(baos, (MapTree) object);
            OutputStream fos = GaBIEn.getOutFile(str);
            if (fos == null)
                throw new IOException("Unable to open a file.");
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".ldb")) {
            DatabaseIO.writeLdb(baos, (Database) object);
            OutputStream fos = GaBIEn.getOutFile(str);
            if (fos == null)
                throw new IOException("Unable to open a file.");
            baos.writeTo(fos);
            fos.close();
            return;
        }
        if (filename.endsWith(".lsd")) {
            SaveDataIO.writeLsd(baos, (Save) object);
            OutputStream fos = GaBIEn.getOutFile(str);
            if (fos == null)
                throw new IOException("Unable to open a file.");
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
