/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.*;

import static r48.schema.AggregateSchemaElement.hookButtonForPressPreserve;

/**
 * Generic string blob (no compression on this one)
 * Created on 2/16/17.
 */
public class StringBlobSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final String fpath = AppMain.rootPath + "r48.edit.txt";

        UITextButton importer = new UITextButton(FontSizes.blobTextHeight, TXDB.get("Import"), new Runnable() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream dis = getCompressionInputStream(GaBIEn.getFile(fpath));
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
                    AppMain.launchDialog(TXDB.get("Wasn't able to import.") + "\n" + ioe);
                }
            }
        });
        final Runnable importScrSaveTicker = hookButtonForPressPreserve(path, launcher, this, target, importer, "import");
        return new UISplitterLayout(new UITextButton(FontSizes.blobTextHeight, TXDB.get("Export/Edit"), new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = GaBIEn.getOutFile(fpath);
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
                    if (!GaBIEn.tryStartTextEditor(fpath))
                        AppMain.launchDialog(TXDB.get("Please edit the file 'edit.txt' in the game's directory."));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    AppMain.launchDialog(TXDB.get("Wasn't able to export.") + "\n" + ioe);
                }
            }
        }), importer, false, 1, 2) {
            @Override
            public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
                super.updateAndRender(ox, oy, deltaTime, select, igd);
                importScrSaveTicker.run();
            }
        };
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
        if (IntegerSchemaElement.ensureType(target, '\"', setDefault)) {
            target.strVal = createDefaultByteArray();
            path.changeOccurred(true);
        } else if (target.strVal == null) {
            target.strVal = createDefaultByteArray();
            path.changeOccurred(true);
        }
    }
}
