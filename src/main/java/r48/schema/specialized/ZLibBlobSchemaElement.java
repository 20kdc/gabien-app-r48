/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Kind of hacky that it calls straight out to Desktop,
 * but it's better for convenience,
 * and certainly better to use the user's editor than whatever abomination I could've cooked up.
 * Created on 1/5/17.
 */
public class ZLibBlobSchemaElement extends StringBlobSchemaElement {

    @Override
    protected InputStream getCompressionInputStream(InputStream file) {
        return new DeflaterInputStream(file);
    }

    @Override
    protected InputStream getDecompressionInputStream(byte[] b) {
        return new InflaterInputStream(new ByteArrayInputStream(b));
    }

}
