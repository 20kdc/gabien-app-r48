/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.uslx.vfs.FSBackend;
import gabien.uslx.vfs.impl.DodgyInputWorkaroundFSBackend;
import gabien.uslx.vfs.impl.UnionFSBackend;
import r48.cfg.Config;
import r48.cfg.FontSizes;
import r48.dbs.ATDB;
import r48.dbs.ObjectDB;
import r48.dbs.ObjectInfo;
import r48.dbs.SDB;
import r48.imageio.ImageIOFormat;
import r48.io.IObjectBackend;
import r48.map.systems.IDynobjMapSystem;
import r48.map.systems.MapSystem;
import r48.tr.pages.TrRoot;
import r48.ui.Art;

/**
 * An attempt to move as much as possible out of static variables.
 * The distinction here is that when possible:
 *  AppCore would hypothetically work without a UI
 *  App won't
 * Pulled out of App, 27th February, 2023
 */
public abstract class AppCore {
    public final @NonNull InterlaunchGlobals ilg;
    // Sub-objects
    public final @NonNull Art a;
    public final @NonNull Config c;
    public final @NonNull FontSizes f;
    public final @NonNull TrRoot t;
    // Launch settings
    public final @NonNull EngineDef engine;
    public final @NonNull Charset encoding;
    // Main
    public final @NonNull TimeMachine timeMachine;
    public ObjectDB odb;
    public final SDB sdb;
    public MapSystem system;
    public ImageIOFormat[] imageIOFormats;

    public ATDB[] autoTiles = new ATDB[0];

    /**
     * This is the root FS for the game being worked on.
     * All game-related writing should go here!
     * (This is important in case Android starts getting particularly aggressive.)
     */
    public final @NonNull FSBackend gameRoot;

    /**
     * UnionFS of all game resource directories.
     */
    public final @NonNull UnionFSBackend gameResources;

    public final @NonNull Consumer<String> loadProgress;

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public AppCore(@NonNull InterlaunchGlobals ilg, @NonNull Charset charset, @NonNull EngineDef engine, @NonNull FSBackend rp, @Nullable FSBackend sip, @NonNull Consumer<String> lp) {
        this.ilg = ilg;
        this.encoding = charset;
        a = ilg.a;
        c = ilg.c;
        f = c.f;
        t = ilg.t;
        this.engine = engine;
        gameRoot = new DodgyInputWorkaroundFSBackend(rp);
        if (sip != null) {
            gameResources = new UnionFSBackend(gameRoot, new DodgyInputWorkaroundFSBackend(sip));
        } else {
            gameResources = new UnionFSBackend(gameRoot);
        }
        loadProgress = lp;
        imageIOFormats = ImageIOFormat.initializeFormats(this);

        // time machine should be before data, because data uses time machine for management
        timeMachine = new TimeMachine(this);

        sdb = new SDB(this);

        // initialize everything else that needs initializing, starting with ObjectDB
        IObjectBackend backend = IObjectBackend.Factory.create(gameRoot, engine.odbBackend, engine.dataPath, engine.dataExt);
        odb = new ObjectDB(this, backend);
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

    /**
     * Theoretically an alias for App.ui.launchDialog to be used by ObjectDB.
     */
    public abstract void reportNonCriticalErrorToUser(String r, Throwable ioe);

    public static class Csv {
        public final @NonNull AppCore app;
        public final @NonNull TrRoot T;
        public Csv(@NonNull AppCore app) {
            this.app = app;
            T = app.t;
        }
    }
}
