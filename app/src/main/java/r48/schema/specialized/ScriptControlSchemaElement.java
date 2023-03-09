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
import r48.App;
import r48.RubyIO;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Using the mkxp-oneshot 'standard', imports and exports RGSS scripts.
 * Created on January 28th, 2018.
 */
public class ScriptControlSchemaElement extends SchemaElement {
    public ScriptControlSchemaElement(App app) {
        super(app);
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {

        final UITextButton importer = new UITextButton(app.ts("Import scripts/*.rb"), app.f.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    RubyIO scripts = importScripts();
                    if (scripts != null) {
                        target.setDeepClone(scripts);
                        path.changeOccurred(true);
                    }
                } catch (IOException ioe) {
                    app.ui.launchDialog(app.ts("An IOException occurred."));
                }
            }
        });

        final UITextButton exporter = new UITextButton(app.ts("Export scripts/*.rb"), app.f.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(app.ts("Script export complete!") + "\n");
                    HashSet<String> used = new HashSet<String>();
                    GaBIEn.makeDirectories(PathUtils.autoDetectWindows(app.rootPath + "scripts"));
                    OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(app.rootPath + "scripts/_scripts.txt"));
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
                            sb.append(app.ts("Script name had to be adjusted: "));
                            sb.append(name);
                            sb.append(" -> ");
                            sb.append(name2);
                            sb.append("\n");
                            continue;
                        }
                        sb.append(app.ts("Script could not be written: "));
                        sb.append(name);
                        sb.append("\n");
                    }
                    ps.close();
                    app.ui.launchDialog(sb.toString());
                } catch (IOException ioe) {
                    app.ui.launchDialog(app.ts("Couldn't export scripts due to IOException: ") + ioe);
                }
            }
            private boolean tryWrite(String name, byte[] inflated, HashSet<String> used, PrintStream ps) throws IOException {
                // do anti-collision here
                String oldName = name;
                int counter = 2;
                while (used.contains(name.toLowerCase()))
                    name = oldName + " (" + (counter++) + ")";
                // continue
                OutputStream os2 = GaBIEn.getOutFile(PathUtils.autoDetectWindows(app.rootPath + "scripts/" + name + ".rb"));
                if (os2 == null)
                    return false;
                os2.write(inflated);
                os2.close();
                used.add(name.toLowerCase());
                ps.println(name);
                return true;
            }
        });

        AggregateSchemaElement.hookButtonForPressPreserve(launcher, this, target, importer, "import");

        UISplitterLayout impExp = new UISplitterLayout(exporter, importer, false, 0.5d);

        final UITextBox searchText = new UITextBox("", app.f.schemaFieldTextHeight);
        UISplitterLayout search = new UISplitterLayout(searchText, new UITextButton(app.ts("Search"), app.f.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                StringBuilder results = new StringBuilder();
                results.append(app.ts("Search Results:"));
                results.append("\n");
                String searchFor = searchText.text;
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

    private RubyIO importScripts() throws IOException {
        // A particular difference that's going to show up here is that empty-named or #-prefixed files won't get removed.
        // This way, the conversion is bi-directional.
        LinkedList<RubyIO> scripts = new LinkedList<RubyIO>();
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(app.rootPath + "scripts/_scripts.txt"));
        if (inp == null) {
            app.ui.launchDialog(app.ts("It appears scripts/_scripts.txt does not exist. It acts as an index."));
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

            RubyIO scr = new RubyIO();
            scr.type = '[';
            scr.arrVal = new RubyIO[3];
            scr.arrVal[0] = new RubyIO().setFX(0);
            scr.arrVal[1] = new RubyIO().setString(s, false);
            DeflaterInputStream def = new DeflaterInputStream(new ByteArrayInputStream(new byte[0]));
            scr.arrVal[2] = new RubyIO().setString(StringBlobSchemaElement.readStream(def), "UTF-8");
            def.close();
            if (ok) {
                byte[] data = loadScript(s);
                if (data == null) {
                    app.ui.launchDialog(app.ts("Script missing: ") + s);
                    return null;
                }
                scr.arrVal[2].putBuffer(data);
            }
            scripts.add(scr);
        }
        br.close();
        RubyIO scriptsArrayFinal = new RubyIO();
        scriptsArrayFinal.type = '[';
        scriptsArrayFinal.arrVal = scripts.toArray(new RubyIO[0]);
        return scriptsArrayFinal;
    }

    private byte[] loadScript(String s) throws IOException {
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(app.rootPath + "scripts/" + s + ".rb"));
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
