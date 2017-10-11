/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.files;

import r48.RubyIO;
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
    public static RubyIO readLmt(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(R2kUtil.readLcfBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapTree"))
            throw new IOException("Not an LcfMapTree");
        // Try to follow the standard...
        MapTree mu = new MapTree();
        mu.importData(fis);
        return mu.asRIO();
    }

    public static void writeLmt(OutputStream fos, RubyIO rio) throws IOException {
        byte[] d = R2kUtil.encodeLcfString("LcfMapTree");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        MapTree db = new MapTree();
        db.fromRIO(rio);
        db.exportData(fos);
    }
}
