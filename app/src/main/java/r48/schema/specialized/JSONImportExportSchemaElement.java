/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.io.JsonObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.minivm.fn.MVMFn;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Import/export of JSON files for game translators.
 * Created on 14th April 2023.
 */
public class JSONImportExportSchemaElement extends SchemaElement.Leaf {
    public final FF0 importText, exportText;
    public final MVMFn importFn, exportFn;
    public final EmbedDataKey<Boolean> buttonEDKey = new EmbedDataKey<>();

    public JSONImportExportSchemaElement(App app, FF0 iT, MVMFn iF, FF0 eT, MVMFn eF) {
        super(app);
        importText = iT;
        importFn = iF;
        exportText = eT;
        exportFn = eF;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {

        final UITextButton importer = new UITextButton(importText.r(), app.f.schemaFieldTH, () -> {
            GaBIEn.startFileBrowser(importText.r(), false, "", (fn) -> {
                if (fn != null) {
                    try (InputStream inp = GaBIEn.getInFile(fn)) {
                        importFn.clDirect(target, JsonObjectBackend.loadJSONFromStream(inp));
                        path.changeOccurred(false);
                    } catch (Exception ioe) {
                        app.ui.launchDialog(ioe);
                    }
                }
            });
        });

        final UITextButton exporter = new UITextButton(exportText.r(), app.f.schemaFieldTH, () -> {
            GaBIEn.startFileBrowser(exportText.r(), true, "", (fn) -> {
                if (fn != null) {
                    try (OutputStream oup = GaBIEn.getOutFile(fn)) {
                        IRIO tmp = new IRIOGeneric(StandardCharsets.UTF_8);
                        exportFn.clDirect(target, tmp);
                        JsonObjectBackend.saveJSONToStream(oup, tmp);
                    } catch (Exception ioe) {
                        app.ui.launchDialog(ioe);
                    }
                }
            });
        });

        AggregateSchemaElement.hookButtonForPressPreserve(launcher, target, importer, buttonEDKey);

        return new UISplitterLayout(exporter, importer, false, 0.5d);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {

    }
}
