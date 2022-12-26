/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.files;

import r48.io.IntUtils;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.MapUnit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Just a namespace-like for map IO functions.
 * <p/>
 * NOTES ABOUT ALL THIS STUFF
 * Ok, so basically, the map format is designed with properties in mind.
 * You know, similar to the Ruby format in that they can omit properties.
 * However, properties are referred to by object-type-specific indexes.
 * Also, these properties are, AFAIK, always in order.
 * The "end property" has an index of 0.
 * <p/>
 * I would assume this applies to the other formats as well.
 * <p/>
 * Created on 30/05/17.
 */
public class MapIO {
    public static MapUnit readLmu(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapUnit"))
            System.err.println("Loading a file which pretends to be an LCF map but says " + magic);
        // Try to follow the standard...
        MapUnit mu = new MapUnit();
        mu.importData(fis);
        return mu;
    }

    public static void writeLmu(OutputStream fos, MapUnit rio) throws IOException {
        byte[] d = R2kUtil.encodeLcfString("LcfMapUnit");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        rio.exportData(fos);
    }
}
