/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import gabien.ui.ISupplier;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Let's just hope this all works out.
 * Created on 31/05/17.
 */
public class SparseArrayR2kInterpretable {
    public static void importData(HashMap<Integer, ?> map, ISupplier constructor, InputStream bais) throws IOException {
        map.clear();
        int entries = R2kUtil.readLcfVLI(bais);
        for (int i = 0; i < entries; i++) {
            int k = R2kUtil.readLcfVLI(bais);
            IR2kInterpretable target = (IR2kInterpretable) constructor.get();
            try {
                target.importData(bais);
            } catch (IOException e) {
                throw new IOException("In element " + i, e);
            } catch (RuntimeException e) {
                throw new RuntimeException("In element " + i, e);
            }
            // Incredibly unsafe but callers need this to reduce complexity.
            // One of these warnings vs. many warnings all over the place,
            //  all over nothing.
            ((HashMap) map).put(k, target);
        }
    }

    public static void exportData(HashMap<Integer, ?> map, OutputStream baos) throws IOException {
        LinkedList<Integer> sort = new LinkedList<Integer>(map.keySet());
        Collections.sort(sort);
        R2kUtil.writeLcfVLI(baos, sort.size());
        for (Integer i : sort) {
            R2kUtil.writeLcfVLI(baos, i);
            ((IR2kInterpretable) map.get(i)).exportData(baos);
        }
    }
}
