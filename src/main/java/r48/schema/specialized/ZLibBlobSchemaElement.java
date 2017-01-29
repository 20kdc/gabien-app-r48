/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
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
public class ZLibBlobSchemaElement implements ISchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        return new UIHHalfsplit(1, 2, new UITextButton(false, "Export/Edit", new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = GaBIEn.getOutFile("edit.rb");
                    InflaterInputStream dis = new InflaterInputStream(new ByteArrayInputStream(target.strVal));
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
        }), new UITextButton(false, "Import", new Runnable() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DeflaterInputStream dis = new DeflaterInputStream(GaBIEn.getFile("edit.rb"));
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

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", false).height;
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
