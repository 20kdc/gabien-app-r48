/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.IPeripherals;
import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.*;
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
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {

        final UITextButton importer = new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Import scripts/*.rb"), new Runnable() {
            @Override
            public void run() {
                try {
                    RubyIO scripts = importScripts();
                    if (scripts != null) {
                        target.setShallowClone(scripts);
                        path.changeOccurred(true);
                    }
                } catch (IOException ioe) {
                    AppMain.launchDialog(TXDB.get("An IOException occurred."));
                }
            }
        });

        final UITextButton exporter = new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Export scripts/*.rb"), new Runnable() {
            @Override
            public void run() {
                try {
                    HashSet<String> used = new HashSet<String>();
                    GaBIEn.makeDirectories(PathUtils.autoDetectWindows(AppMain.rootPath + "scripts"));
                    OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "scripts/_scripts.txt"));
                    PrintStream ps = new PrintStream(os, false, "UTF-8");
                    for (int i = 0; i < target.arrVal.length; i++) {
                        String name = target.arrVal[i].arrVal[1].decString();
                        // Just in case...
                        name = name.replace(':', '_');
                        name = name.replace('/', '_');
                        name = name.replace('\\', '_');
                        // need to inflate
                        byte[] inflated = StringBlobSchemaElement.readStream(new InflaterInputStream(new ByteArrayInputStream(target.arrVal[i].arrVal[2].strVal)));
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

                        if (!disable)
                            if (name.equals(""))
                                name = "UNTITLED";
                        // This approach isn't the same, but should be theoretically more reliable. Theoretically.
                        if (!disable) {
                            String oldName = name;
                            int counter = 2;
                            while (used.contains(name.toLowerCase()))
                                name = oldName + " (" + (counter++) + ")";
                            used.add(name.toLowerCase());
                        } else if (!name.equals("")) {
                            // Name prefix.
                            name = "# " + name;
                        }

                        ps.println(name);
                        if (!disable) {
                            OutputStream os2 = GaBIEn.getOutFile(PathUtils.autoDetectWindows("scripts/" + name + ".rb"));
                            os2.write(inflated);
                            os2.close();
                        }
                    }
                    ps.close();
                    AppMain.launchDialog(TXDB.get("Script export complete!"));
                } catch (IOException ioe) {
                    AppMain.launchDialog(TXDB.get("Couldn't export scripts due to IOException: ") + ioe);
                }
            }
        });

        final Runnable importScrSaveTicker = AggregateSchemaElement.hookButtonForPressPreserve(path, launcher, this, target, importer, "import");

        return new UISplitterLayout(exporter, importer, false, 0.5d) {
            @Override
            public void update(double deltaTime) {
                super.update(deltaTime);
                importScrSaveTicker.run();
            }
        };
    }

    private RubyIO importScripts() throws IOException {
        // A particular difference that's going to show up here is that empty-named or #-prefixed files won't get removed.
        // This way, the conversion is bi-directional.
        LinkedList<RubyIO> scripts = new LinkedList<RubyIO>();
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(AppMain.rootPath + "scripts/_scripts.txt"));
        if (inp == null) {
            AppMain.launchDialog(TXDB.get("It appears scripts/_scripts.txt does not exist. It acts as an index."));
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
            scr.arrVal[2] = new RubyIO();
            scr.arrVal[2].type = '"';
            DeflaterInputStream def = new DeflaterInputStream(new ByteArrayInputStream(new byte[0]));
            scr.arrVal[2].strVal = StringBlobSchemaElement.readStream(def);
            def.close();
            if (ok) {
                scr.arrVal[2].strVal = loadScript(s);
                if (scr.arrVal[2].strVal == null) {
                    AppMain.launchDialog(TXDB.get("Script missing: ") + s);
                    return null;
                }
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
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(AppMain.rootPath + "scripts/" + s + ".rb"));
        if (inp == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream def = new DeflaterOutputStream(baos);
        StringBlobSchemaElement.copyStream(inp, def);
        def.close();
        return baos.toByteArray();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {

    }
}
