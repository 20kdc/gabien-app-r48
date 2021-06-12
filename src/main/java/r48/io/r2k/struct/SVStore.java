/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.io.IntUtils;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class SVStore extends StringR2kStruct {
    @Override
    public void importData(InputStream bais) throws IOException {
        SVStore.importTermlike(bais, new int[] {1}, new StringR2kStruct[] {this});
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        SVStore.exportTermlike(baos, new int[] {1}, new StringR2kStruct[] {this});
        return false;
    }

    public static void importTermlike(InputStream bais, int[] map, StringR2kStruct[] termArray) throws IOException {
        for (int i = 0; i < termArray.length; i++)
            if (termArray[i] == null)
                termArray[i] = new StringR2kStruct();
        while (true) {
            int idx = R2kUtil.readLcfVLI(bais);
            if (idx == 0)
                break;
            int len = R2kUtil.readLcfVLI(bais);
            byte[] data = IntUtils.readBytes(bais, len);
            boolean found = false;
            for (int i = 0; i < map.length; i++) {
                if (map[i] == idx) {
                    termArray[i].data = data;
                    found = true;
                    break;
                }
            }
            if (!found)
                System.err.println("UNKNOWN TERMLIKE CHUNK: " + idx);
        }
    }

    public static void exportTermlike(OutputStream baos, int[] map, StringR2kStruct[] termArray) throws IOException {
        for (int i = 0; i < termArray.length; i++) {
            R2kUtil.writeLcfVLI(baos, map[i]);
            R2kUtil.writeLcfVLI(baos, termArray[i].data.length);
            baos.write(termArray[i].data);
        }
        baos.write(0);
    }
}
