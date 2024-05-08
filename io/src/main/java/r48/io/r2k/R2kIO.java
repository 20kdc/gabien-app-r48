/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k;

import r48.io.IntUtils;
import r48.io.data.DMContext;
import r48.io.r2k.obj.MapUnit;
import r48.io.r2k.obj.Save;
import r48.io.r2k.obj.ldb.Database;
import r48.io.r2k.struct.MapTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Save editing is a much more visible business.
 * Created just after midnight, 23rd of November 2017.
 * Merged 6th April 2023 from:
 *  DatabaseIO (Created on 01/06/17)
 *  MapIO (Created on 30/05/17)
 *  MapTreeIO (Created on 31/05/17)
 */
public class R2kIO {
    public static Save readLsd(DMContext dm2c, InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(dm2c, IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfSaveData"))
            System.err.println("Loading a file which pretends to be an LCF save file but says " + magic);
        Save mu = new Save(dm2c);
        mu.importData(fis);
        return mu;
    }

    public static void writeLsd(OutputStream fos, Save rio) throws IOException {
        byte[] d = R2kUtil.encodeLcfString(rio.dm2Ctx, "LcfSaveData");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        rio.exportData(fos);
    }

    public static Database readLdb(DMContext context, InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(context, IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfDataBase"))
            System.err.println("Loading a file which pretends to be an LCF database but says " + magic);
        // Try to follow the standard...
        Database mu = new Database(context);
        mu.importData(fis);
        return mu;
    }

    public static void writeLdb(OutputStream fos, Database db) throws IOException {
        byte[] d = R2kUtil.encodeLcfString(db.dm2Ctx, "LcfDataBase");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        db.exportData(fos);
    }

    public static MapUnit readLmu(DMContext dm2c, InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(dm2c, IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapUnit"))
            System.err.println("Loading a file which pretends to be an LCF map but says " + magic);
        // Try to follow the standard...
        MapUnit mu = new MapUnit(dm2c);
        mu.importData(fis);
        return mu;
    }

    public static void writeLmu(OutputStream fos, MapUnit rio) throws IOException {
        byte[] d = R2kUtil.encodeLcfString(rio.dm2Ctx, "LcfMapUnit");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        rio.exportData(fos);
    }

    public static MapTree readLmt(DMContext dm2c, InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(dm2c, IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapTree"))
            System.err.println("Loading a file which pretends to be an LCF map tree but says " + magic);
        // Try to follow the standard...
        MapTree mu = new MapTree(dm2c);
        mu.importData(fis);
        return mu;
    }

    public static void writeLmt(OutputStream fos, MapTree db) throws IOException {
        byte[] d = R2kUtil.encodeLcfString(db.dm2Ctx, "LcfMapTree");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        db.exportData(fos);
    }
}
