/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * just doing some restructuring...
 * Created on 01/06/17.
 */
public interface IR2kInterpretable {
    void importData(InputStream bais) throws IOException;

    // If this returns true, the chunk is omitted.
    boolean exportData(OutputStream baos) throws IOException;
}
