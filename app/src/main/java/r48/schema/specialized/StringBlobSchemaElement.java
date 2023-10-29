/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.App;
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
public class StringBlobSchemaElement extends SchemaElement.Leaf {
    public StringBlobSchemaElement(App app) {
        super(app);
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final String fpath = Application.BRAND + "/r48.edit.txt";

        UITextButton importer = new UITextButton(T.s.bImport, app.f.blobTH, new Runnable() {
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
                    app.ui.launchDialog(T.s.scx_impFail + "\n" + ioe);
                }
            }
        });
        AggregateSchemaElement.hookButtonForPressPreserve(launcher, this, target, importer, "import");
        UISplitterLayout usl = new UISplitterLayout(new UITextButton(T.s.bExportEdit, app.f.blobTH, new Runnable() {
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
                        app.ui.launchDialog(T.s.scx_editorFail);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    app.ui.launchDialog(T.s.scx_fail + "\n" + ioe);
                }
            }
        }), importer, false, 0.5d); 
        return new UISplitterLayout(usl, new UITextButton(T.s.bEditHere, app.f.blobTH, new Runnable() {
            @Override
            public void run() {
                final UITextBox utb = new UITextBox("", app.f.schemaFieldTH).setMultiLine();
                Runnable update = () -> {
                    try {
                        utb.setText(readContentString(target));
                    } catch (IOException e) {
                        app.ui.launchDialog(T.s.dErrNoRead, e);
                    }
                };
                update.run();
                UIElement ui = new UISplitterLayout(utb, new UITextButton(T.g.bConfirm, app.f.schemaFieldTH, () -> {
                    try {
                        writeContentString(target, utb.getText());
                    } catch (IOException e) {
                        app.ui.launchDialog(T.s.dErrNoWrite, e);
                        return;
                    }
                    path.changeOccurred(false);
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
        return new String(baos.toByteArray(), app.encoding);
    }
    private void writeContentString(IRIO target, String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(getCompressionInputStream(new ByteArrayInputStream(text.getBytes(app.encoding))), baos);
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
