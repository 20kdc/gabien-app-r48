/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.files;

import r48.RubyIO;
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
    public static RubyIO readLmu(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(R2kUtil.readLcfBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapUnit"))
            throw new IOException("Not an LcfMapUnit");
        // Try to follow the standard...
        MapUnit mu = new MapUnit();
        mu.importData(fis);
        return mu.asRIO();
    }

    public static void writeLmu(OutputStream fos, RubyIO rio) throws IOException {
        byte[] d = R2kUtil.encodeLcfString("LcfMapUnit");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        MapUnit db = new MapUnit();
        db.fromRIO(rio);
        db.exportData(fos);
    }
}
