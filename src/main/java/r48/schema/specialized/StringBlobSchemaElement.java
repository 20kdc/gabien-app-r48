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
import r48.schema.SchemaElement;
import r48.schema.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIHHalfsplit;

import java.awt.*;
import java.io.*;

/**
 * Generic string blob (no compression on this one)
 * Created on 2/16/17.
 */
public class StringBlobSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        return new UIHHalfsplit(1, 2, new UITextButton(FontSizes.blobTextHeight, "Export/Edit", new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = GaBIEn.getOutFile("edit.rb");
                    InputStream dis = getDecompressionInputStream(target.strVal);
                    byte[] block = new byte[512];
                    while (true) {
                        int r = dis.read(block);
                        if (r <= 0)
                            break;
                        os.write(block, 0, r);
                    }
                    dis.close();
                    os.close();
                    Desktop.getDesktop().open(new File("edit.rb"));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }), new UITextButton(FontSizes.blobTextHeight, "Import", new Runnable() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream dis = getCompressionInputStream(GaBIEn.getFile("edit.rb"));
                    byte[] block = new byte[512];
                    while (true) {
                        int r = dis.read(block);
                        if (r <= 0)
                            break;
                        baos.write(block, 0, r);
                    }
                    dis.close();
                    target.strVal = baos.toByteArray();
                    path.changeOccurred(false);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }));
    }

    protected InputStream getCompressionInputStream(InputStream file) {
        return file;
    }

    protected InputStream getDecompressionInputStream(byte[] b) {
        return new ByteArrayInputStream(b);
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.blobTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, '\"', setDefault)) {
            target.strVal = new byte[0];
            path.changeOccurred(true);
        } else if (target.strVal == null) {
            target.strVal = new byte[0];
            path.changeOccurred(true);
        }
    }
}
