/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.uslx.append.IConsumer;
import r48.RubyIO;
import r48.cfg.Config;
import r48.cfg.FontSizes;
import r48.dbs.ATDB;
import r48.dbs.FormatSyntax;
import r48.dbs.ObjectDB;
import r48.dbs.ObjectInfo;
import r48.dbs.SDB;
import r48.imageio.ImageIOFormat;
import r48.io.data.IRIO;
import r48.map.systems.IDynobjMapSystem;
import r48.map.systems.MapSystem;
import r48.schema.specialized.IMagicalBinder;
import r48.tr.ITranslator;
import r48.tr.LanguageList;
import r48.tr.NullTranslator;
import r48.tr.Translator;
import r48.tr.pages.TrRoot;

/**
 * An attempt to move as much as possible out of static variables.
 * Pulled out of App, 27th February, 2023
 */
public class AppCore {
    public final @NonNull InterlaunchGlobals ilg;
    // Sub-objects
    public final @NonNull Config c;
    public final @NonNull FontSizes f;
    public final @NonNull TrRoot t;
    // Main
    public final @NonNull ITranslator d; // dynamic string translation
    public ObjectDB odb;
    public SDB sdb;
    public FormatSyntax fmt;
    public MapSystem system;
    public ImageIOFormat[] imageIOFormats;

    // All magical bindings in use
    public WeakHashMap<IRIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>> magicalBindingCache = new WeakHashMap<>();

    public ATDB[] autoTiles = new ATDB[0];
    public String odbBackend = "<you forgot to select a backend>";
    public final @NonNull String rootPath;
    public final @Nullable String secondaryImagePath;
    // Null system backend will always "work"
    public String sysBackend = "null";
    public String dataPath = "";
    public String dataExt = "";

    public final @NonNull IConsumer<String> loadProgress;

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public AppCore(@NonNull InterlaunchGlobals ilg, @NonNull String gp, @NonNull String rp, @Nullable String sip, @NonNull IConsumer<String> lp) {
        this.ilg = ilg;
        c = ilg.c;
        f = c.f;
        t = ilg.t;
        rootPath = rp;
        secondaryImagePath = sip;
        loadProgress = lp;
        fmt = new FormatSyntax(this);
        imageIOFormats = ImageIOFormat.initializeFormats(this);
        d = createGPTranslatorForLang(c.language, gp);
    }

    /**
     * Legacy static translation
     */
    public String ts(String text) {
        return d.tr("r48", text);
    }

    /**
     * Legacy dynamic translation
     */
    public String td(String context, String text) {
        return d.tr(context.replace('/', '-'), text);
    }

    /**
     * Legacy dynamic translation
     */
    public String trExUnderscore(String context, String text) {
        if (text.equals("_"))
            return "_";
        return td(context, text);
    }

    public void performTranslatorDump(String fnPrefix, String ctxPrefix) {
        d.dump(fnPrefix, ctxPrefix);
    }

    private static ITranslator createGPTranslatorForLang(String lang, String gp) {
        ITranslator currentTranslator;
        if (lang.equals(LanguageList.hardcodedLang)) {
            currentTranslator = new NullTranslator();
        } else {
            currentTranslator = new Translator(lang);
        }
        currentTranslator.read("Systerms/" + lang + ".txt", "r48/");
        currentTranslator.read("Systerms/L-" + lang + ".txt", "launcher/");
        try {
            currentTranslator.read(gp + "Lang" + lang + ".txt", "SDB@");
        } catch (Exception e) {
        }
        try {
            currentTranslator.read(gp + "Cmtx" + lang + ".txt", "CMDB@");
        } catch (Exception e) {
        }
        return currentTranslator;
    }

    // Attempts to ascertain all known objects
    public LinkedList<String> getAllObjects() {
        // anything loaded gets added (this allows some bypass of the mechanism)
        HashSet<String> mainSet = new HashSet<String>(odb.objectMap.keySet());
        for (ObjectInfo oi : sdb.listFileDefs())
            mainSet.add(oi.idName);
        if (system instanceof IDynobjMapSystem) {
            IDynobjMapSystem idms = (IDynobjMapSystem) system;
            for (ObjectInfo dobj : idms.getDynamicObjects())
                mainSet.add(dobj.idName);
        }
        return new LinkedList<String>(mainSet);
    }

    // Attempts to ascertain all known objects
    public LinkedList<ObjectInfo> getObjectInfos() {
        LinkedList<ObjectInfo> oi = sdb.listFileDefs();
        if (system instanceof IDynobjMapSystem) {
            IDynobjMapSystem idms = (IDynobjMapSystem) system;
            for (ObjectInfo dobj : idms.getDynamicObjects())
                oi.add(dobj);
        }
        return oi;
    }

    public static class Csv {
        public final @NonNull AppCore app;
        public final @NonNull TrRoot T;
        public Csv(@NonNull AppCore app) {
            this.app = app;
            T = app.t;
        }
    }
}
