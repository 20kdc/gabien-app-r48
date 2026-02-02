/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

import r48.R48;

/**
 * Kind of hacky that it calls straight out to Desktop,
 * but it's better for convenience,
 * and certainly better to use the user's editor than whatever abomination I could've cooked up.
 * Created on 1/5/17.
 */
public class ZLibBlobSchemaElement extends StringBlobSchemaElement {

    public ZLibBlobSchemaElement(R48 app) {
        super(app);
    }

    @Override
    protected InputStream getCompressionInputStream(InputStream file) {
        return new DeflaterInputStream(file);
    }

    @Override
    protected InputStream getDecompressionInputStream(byte[] b) {
        return new InflaterInputStream(new ByteArrayInputStream(b));
    }

    @Override
    protected byte[] createDefaultByteArray() {
        try {
            DeflaterInputStream dis = new DeflaterInputStream(new ByteArrayInputStream(new byte[0]));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] block = new byte[512];
            while (true) {
                int r = dis.read(block);
                if (r <= 0)
                    break;
                baos.write(block, 0, r);
            }
            dis.close();
            return baos.toByteArray();
        } catch (IOException ie) {
            ie.printStackTrace();
            return new byte[0];
        }
    }
}
