/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.files;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.Database;
import r48.io.r2k.obj.MapUnit;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created on 01/06/17.
 */
public class DatabaseIO {
    public static RubyIO readLdb(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(R2kUtil.readLcfBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfDataBase"))
            throw new IOException("Not an LcfDataBase");
        // Try to follow the standard...
        Database mu = new Database();
        mu.importData(fis);
        return mu.asRIO();
    }
}
