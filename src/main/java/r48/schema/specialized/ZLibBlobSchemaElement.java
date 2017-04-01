/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
