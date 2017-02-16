/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIHHalfsplit;

import java.awt.*;
import java.io.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Kind of hacky that it calls straight out to Desktop,
 *  but it's better for convenience,
 *  and certainly better to use the user's editor than whatever abomination I could've cooked up.
 * Created on 1/5/17.
 */
public class ZLibBlobSchemaElement extends StringBlobSchemaElement {

    @Override
    protected InputStream getCompressionInputStream(InputStream file) {
        return new DeflaterInputStream(file);
    }

    @Override
    protected InputStream getDecompressionInputStream(byte[] b) {
        return new DeflaterInputStream(new ByteArrayInputStream(b));
    }

}
