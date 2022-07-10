/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creates a size chunk. The size chunk will be exported after the primary chunk,
 * but will appear in the stream before the original chunk.
 * Created on December 05, 2018.
 */
public interface IR2kSizable {
    void exportSize(OutputStream baos) throws IOException;
}
