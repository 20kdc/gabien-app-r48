/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.files;

import r48.io.IntUtils;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.ldb.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 01/06/17.
 */
public class DatabaseIO {
    public static Database readLdb(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(IntUtils.readBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfDataBase"))
            System.err.println("Loading a file which pretends to be an LCF database but says " + magic);
        // Try to follow the standard...
        Database mu = new Database();
        mu.importData(fis);
        return mu;
    }

    public static void writeLdb(OutputStream fos, Database db) throws IOException {
        byte[] d = R2kUtil.encodeLcfString("LcfDataBase");
        R2kUtil.writeLcfVLI(fos, d.length);
        fos.write(d);
        db.exportData(fos);
    }
}
