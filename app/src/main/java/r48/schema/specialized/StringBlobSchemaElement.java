/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextBox;
import gabien.ui.UITextButton;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.App;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
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
    public StringBlobSchemaElement(App app) {
        super(app);
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final String fpath = Application.BRAND + "/r48.edit.txt";

        UITextButton importer = new UITextButton(T.z.l154, app.f.blobTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    AdHocSaveLoad.prepare();
                    InputStream dis = getCompressionInputStream(GaBIEn.getInFile(fpath));
                    target.putBuffer(readStream(dis));
                    dis.close();
                    path.changeOccurred(false);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    app.ui.launchDialog(T.z.l155 + "\n" + ioe);
                }
            }
        });
        AggregateSchemaElement.hookButtonForPressPreserve(launcher, this, target, importer, "import");
        UISplitterLayout usl = new UISplitterLayout(new UITextButton(T.z.l156, app.f.blobTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    AdHocSaveLoad.prepare();
                    OutputStream os = GaBIEn.getOutFile(fpath);
                    InputStream dis = getDecompressionInputStream(target.getBuffer());
                    copyStream(dis, os);
                    dis.close();
                    os.close();
                    if (!GaBIEn.tryStartTextEditor(fpath))
                        app.ui.launchDialog(T.z.l157);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    app.ui.launchDialog(T.z.l158 + "\n" + ioe);
                }
            }
        }), importer, false, 0.5d); 
        return new UISplitterLayout(usl, new UITextButton(T.z.l159, app.f.blobTextHeight, new Runnable() {
            @Override
            public void run() {
                final UITextBox utb = new UITextBox("", app.f.schemaFieldTextHeight).setMultiLine();
                Runnable update = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            utb.text = readContentString(target);
                        } catch (IOException e) {
                            app.ui.launchDialog(T.z.l160, e);
                        }
                    }
                };
                update.run();
                UIElement ui = new UISplitterLayout(utb, new UITextButton(T.z.l11, app.f.schemaFieldTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeContentString(target, utb.text);
                        } catch (IOException e) {
                            app.ui.launchDialog(T.z.l161, e);
                            return;
                        }
                        path.changeOccurred(false);
                    }
                }), true, 1);
                launcher.pushObject(path.newWindow(new TempDialogSchemaChoice(app, ui, update, path), target));
            }
        }), false, 1);
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

    private String readContentString(IRIO target) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(getDecompressionInputStream(target.getBuffer()), baos);
        return new String(baos.toByteArray(), IObjectBackend.Factory.encoding);
    }
    private void writeContentString(IRIO target, String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(getCompressionInputStream(new ByteArrayInputStream(text.getBytes(IObjectBackend.Factory.encoding))), baos);
        target.putBuffer(baos.toByteArray());
    }

    protected byte[] createDefaultByteArray() {
        return new byte[0];
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (SchemaElement.checkType(target, '\"', null, setDefault)) {
            target.putBuffer(createDefaultByteArray());
            path.changeOccurred(true);
        }
    }
}
