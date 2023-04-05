/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.files;

import r48.io.IntUtils;
import r48.io.data.DM2Context;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.struct.MapTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * It's like MapIO, but not!
 * Created on 31/05/17.
 */
public class MapTreeIO {
    public static MapTree readLmt(DM2Context dm2c, InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(dm2c, IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapTree"))
            System.err.println("Loading a file which pretends to be an LCF map tree but says " + magic);
        // Try to follow the standard...
        MapTree mu = new MapTree(dm2c);
        mu.importData(fis);
        return mu;
    }

    public static void writeLmt(OutputStream fos, MapTree db) throws IOException {
        byte[] d = R2kUtil.encodeLcfString(db.context, "LcfMapTree");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        db.exportData(fos);
    }
}
