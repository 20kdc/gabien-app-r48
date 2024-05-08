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
import gabien.uslx.vfs.FSBackend;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Using the mkxp-oneshot 'standard', imports and exports RGSS scripts.
 * Created on January 28th, 2018.
 */
public class ScriptControlSchemaElement extends SchemaElement.Leaf {
    public final EmbedDataKey<Boolean> buttonEDKey = new EmbedDataKey<>();

    public ScriptControlSchemaElement(App app) {
        super(app);
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {

        final UITextButton importer = new UITextButton(T.s.bImportOS, app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                try {
                    IRIO scripts = importScripts();
                    if (scripts != null) {
                        target.setDeepClone(scripts);
                        path.changeOccurred(true);
                    }
                } catch (Exception ioe) {
                    app.ui.launchDialog(ioe);
                }
            }
        });

        final UITextButton exporter = new UITextButton(T.s.bExportOS, app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(T.s.scx_done + "\n");
                    HashSet<String> used = new HashSet<String>();
                    FSBackend scripts = app.gameRoot.into("scripts");
                    scripts.mkdirs();
                    OutputStream os = GaBIEn.getOutFile(scripts.into("_scripts.txt"));
                    PrintStream ps = new PrintStream(os, false, "UTF-8");
                    int alen = target.getALen();
                    for (int i = 0; i < alen; i++) {
                        // need to inflate
                        byte[] inflated = StringBlobSchemaElement.readStream(new InflaterInputStream(new ByteArrayInputStream(target.getAElem(i).getAElem(2).getBuffer())));
                        // target.arrVal[i].arrVal[2];

                        boolean disable = false;

                        // Define the following cases as "empty scripts". Not totally compatible but should be close enough.
                        if (inflated.length == 0) {
                            disable = true;
                        } else if ((inflated.length == 1) && (inflated[0] == '\n')) {
                            disable = true;
                        } else if ((inflated.length == 2) && (inflated[0] == '\r') && (inflated[1] == '\n')) {
                            disable = true;
                        }

                        String name = target.getAElem(i).getAElem(1).decString();

                        // If it's disabled, just deal with it
                        if (disable) {
                            if (!name.equals("")) {
                                ps.println("# " + name);
                            } else {
                                ps.println("");
                            }
                            continue;
                        }

                        // do early changes to name
                        name = name.replace(':', '_');
                        name = name.replace('/', '_');
                        name = name.replace('\\', '_');
                        if (name.equals(""))
                            name = "UNTITLED";

                        // try with current name...
                        if (tryWrite(name, inflated, used, ps))
                            continue;

                        // ok, that didn't work, try with altered name
                        char[] adjusted = name.toCharArray();
                        for (int j = 0; j < adjusted.length; j++)
                            if (!Character.isAlphabetic(adjusted[j]))
                                adjusted[j] = '_';

                        String name2 = new String(adjusted);
                        if (tryWrite(name2, inflated, used, ps)) {
                            sb.append(T.s.scx_ch);
                            sb.append(name);
                            sb.append(" -> ");
                            sb.append(name2);
                            sb.append("\n");
                            continue;
                        }
                        sb.append(T.s.scx_wf);
                        sb.append(name);
                        sb.append("\n");
                    }
                    ps.close();
                    app.ui.launchDialog(sb.toString());
                } catch (Exception ioe) {
                    app.ui.launchDialog(ioe);
                }
            }
            private boolean tryWrite(String name, byte[] inflated, HashSet<String> used, PrintStream ps) throws IOException {
                // do anti-collision here
                String oldName = name;
                int counter = 2;
                while (used.contains(name.toLowerCase()))
                    name = oldName + " (" + (counter++) + ")";
                // continue
                OutputStream os2 = GaBIEn.getOutFile(app.gameRoot.intoPath("scripts/" + name + ".rb"));
                if (os2 == null)
                    return false;
                os2.write(inflated);
                os2.close();
                used.add(name.toLowerCase());
                ps.println(name);
                return true;
            }
        });

        AggregateSchemaElement.hookButtonForPressPreserve(launcher, target, importer, buttonEDKey);

        UISplitterLayout impExp = new UISplitterLayout(exporter, importer, false, 0.5d);

        final UITextBox searchText = new UITextBox("", app.f.schemaFieldTH);
        UISplitterLayout search = new UISplitterLayout(searchText, new UITextButton(T.s.bSearch, app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                StringBuilder results = new StringBuilder();
                results.append(T.s.searchResults);
                results.append("\n");
                String searchFor = searchText.getText();
                int alen = target.getALen();
                for (int i = 0; i < alen; i++) {
                    String name = target.getAElem(i).getAElem(1).decString();
                    // need to inflate
                    byte[] inflated = null;
                    try {
                        inflated = StringBlobSchemaElement.readStream(new InflaterInputStream(new ByteArrayInputStream(target.getAElem(i).getAElem(2).getBuffer())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (inflated == null)
                        continue;
                    String res = new String(inflated, StandardCharsets.UTF_8);
                    if (res.contains(searchFor)) {
                        results.append(i);
                        results.append(": ");
                        results.append(name);
                        results.append("\n");
                    }
                }
                app.ui.launchDialog(results.toString());
            }
        }), true, 0);
        return new UISplitterLayout(impExp, search, true, 0);
    }

    private IRIO importScripts() throws IOException {
        // A particular difference that's going to show up here is that empty-named or #-prefixed files won't get removed.
        // This way, the conversion is bi-directional.
        IRIO scripts = new IRIOGeneric(app.ctxDisposableAppEncoding).setArray();
        InputStream inp = GaBIEn.getInFile(app.gameResources.into("scripts", "_scripts.txt"));
        if (inp == null) {
            app.ui.launchDialog(T.s.scx_noIdx);
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inp, "UTF-8"));
        while (br.ready()) {
            String s = br.readLine();
            boolean ok = true;

            if (s.startsWith("# ")) {
                s = s.substring(2);
                ok = false;
            } else if (s.startsWith("#")) {
                s = s.substring(1);
                ok = false;
            } else if (s.equals("")) {
                ok = false;
            }

            IRIO scr = scripts.addAElem(scripts.getALen());
            scr.setArray(3);
            scr.getAElem(0).setFX(0);
            scr.getAElem(1).setString(s);
            DeflaterInputStream def = new DeflaterInputStream(new ByteArrayInputStream(new byte[0]));
            scr.getAElem(2).setString(StringBlobSchemaElement.readStream(def), StandardCharsets.UTF_8);
            def.close();
            if (ok) {
                byte[] data = loadScript(s);
                if (data == null) {
                    app.ui.launchDialog(T.s.scx_miss + s);
                    return null;
                }
                scr.getAElem(2).putBuffer(data);
            }
        }
        br.close();
        return scripts;
    }

    private byte[] loadScript(String s) throws IOException {
        InputStream inp = GaBIEn.getInFile(app.gameResources.intoPath("scripts/" + s + ".rb").parentMkdirs());
        if (inp == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream def = new DeflaterOutputStream(baos);
        StringBlobSchemaElement.copyStream(inp, def);
        def.close();
        return baos.toByteArray();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {

    }
}
