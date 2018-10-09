/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.*;

/**
 * Generic string blob (no compression on this one)
 * Created on 2/16/17.
 */
public class StringBlobSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final String fpath = Application.BRAND + "/r48.edit.txt";

        UITextButton importer = new UITextButton(TXDB.get("Import"), FontSizes.blobTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    AdHocSaveLoad.prepare();
                    InputStream dis = getCompressionInputStream(GaBIEn.getInFile(fpath));
                    target.strVal = readStream(dis);
                    dis.close();
                    path.changeOccurred(false);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    AppMain.launchDialog(TXDB.get("Wasn't able to import 'r48.edit.txt' from the R48 settings folder.") + "\n" + ioe);
                }
            }
        });
        AggregateSchemaElement.hookButtonForPressPreserve(launcher, this, target, importer, "import");
        return new UISplitterLayout(new UITextButton(TXDB.get("Export/Edit"), FontSizes.blobTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    AdHocSaveLoad.prepare();
                    OutputStream os = GaBIEn.getOutFile(fpath);
                    InputStream dis = getDecompressionInputStream(target.strVal);
                    copyStream(dis, os);
                    dis.close();
                    os.close();
                    if (!GaBIEn.tryStartTextEditor(fpath))
                        AppMain.launchDialog(TXDB.get("Unable to start the editor! Wrote to the file 'r48.edit.txt' in the R48 settings folder."));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    AppMain.launchDialog(TXDB.get("Wasn't able to export.") + "\n" + ioe);
                }
            }
        }), importer, false, 0.5d);
    }

    public static byte[] readStream(InputStream dis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] block = new byte[512];
        while (true) {
            int r = dis.read(block);
            if (r <= 0)
                break;
            baos.write(block, 0, r);
        }
        return baos.toByteArray();
    }

    public static void copyStream(InputStream dis, OutputStream os) throws IOException {
        byte[] block = new byte[512];
        while (true) {
            int r = dis.read(block);
            if (r <= 0)
                break;
            os.write(block, 0, r);
        }
    }

    protected InputStream getCompressionInputStream(InputStream file) {
        return file;
    }

    protected InputStream getDecompressionInputStream(byte[] b) {
        return new ByteArrayInputStream(b);
    }

    protected byte[] createDefaultByteArray() {
        return new byte[0];
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (SchemaElement.ensureType(target, '\"', setDefault)) {
            target.strVal = createDefaultByteArray();
            path.changeOccurred(true);
        } else if (target.strVal == null) {
            target.strVal = createDefaultByteArray();
            path.changeOccurred(true);
        }
    }
}
