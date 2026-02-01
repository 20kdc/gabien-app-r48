/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;
import datum.DatumSymbol;
import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UIElement.UIPanel;
import gabien.ui.UIElement.UIProxy;
import gabien.uslx.vfs.FSBackend;
import gabien.uslx.vfs.impl.DodgyInputWorkaroundFSBackend;
import gabien.uslx.vfs.impl.UnionFSBackend;
import gabienapp.PleaseFailBrutally;
import r48.app.AppNewProject;
import r48.app.AppUI;
import r48.app.IAppAsSeenByLauncher;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.cfg.FontSizes;
import r48.dbs.CMDBDB;
import r48.dbs.ObjectDB;
import r48.dbs.ObjectInfo;
import r48.dbs.RPGCommand;
import r48.dbs.SDB;
import r48.dbs.SDBHelpers;
import r48.gameinfo.ATDB;
import r48.gameinfo.EngineDef;
import r48.imageio.ImageIOFormat;
import r48.io.data.DMContext;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.undoredo.DMChangeTracker;
import r48.io.undoredo.TimeMachine;
import r48.map.StuffRenderer;
import r48.map.systems.MapSystem;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMSlot;
import r48.minivm.fn.MVMR48AppLibraries;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SchemaElementIOP;
import r48.schema.op.BaseSchemaOps;
import r48.schema.op.SchemaOp;
import r48.search.ByCodeCommandClassifier;
import r48.search.CommandTag;
import r48.search.CompoundTextAnalyzer;
import r48.search.ICommandClassifier;
import r48.search.ITextAnalyzer;
import r48.search.ImmutableTextAnalyzerCommandClassifier;
import r48.search.TextAnalyzerCommandClassifier;
import r48.search.TextOperator;
import r48.toolsets.utils.IDChangerEntry;
import r48.tr.DynTrBase;
import r48.tr.IDynTrProxy;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF1;
import r48.tr.pages.TrRoot;
import r48.ui.Art;

/**
 * An attempt to move as much as possible out of static variables.
 * The distinction here is that when possible:
 *  AppCore would hypothetically work without a UI
 *  App won't
 * Created 26th February, 2023
 */
public final class App implements IAppAsSeenByLauncher, IDynTrProxy, TimeMachine.Host, ObjectDB.Host {
    /**
     * Inter-launch globals (art, config, etc.)
     */
    @NonNull
    public final InterlaunchGlobals ilg;

    /**
     * 'Art' (logos, symbols, etc.)
     */
    @NonNull
    public final Art a;

    /**
     * Configuration
     */
    @NonNull
    public final Config c;

    /**
     * Font sizes
     */
    @NonNull
    public final FontSizes f;

    /**
     * Translation root page (copied from ILG)
     */
    @NonNull
    public final TrRoot t;

    /**
     * Engine definition
     */
    @NonNull
    public final EngineDef engine;

    /**
     * Game's encoding
     */
    @NonNull
    public final Charset encoding;

    /**
     * The time machine
     */
    @NonNull
    public final TimeMachine timeMachine;

    /**
     * Object database
     */
    public ObjectDB odb;

    /**
     * Primary D/MVM virtual machine (in-app REPL, Schema, etc.)
     */
    public final MVMEnvR48 vmCtx;

    /**
     * Schema database - the most important thing
     */
    public final SDB sdb;

    /**
     * Map system - defines maps, etc.
     */
    public MapSystem system;

    /**
     * ImageIO formats
     */
    public ImageIOFormat[] imageIOFormats;

    /**
     * Autotile fields
     */
    public ATDB[] autoTiles = new ATDB[0];

    /**
     * This is the root FS for the game being worked on.
     * All game-related writing should go here!
     * (This is important in case Android starts getting particularly aggressive.)
     */
    @NonNull
    public final FSBackend gameRoot;

    /**
     * UnionFS of all game resource directories.
     */
    @NonNull
    public final UnionFSBackend gameResources;

    /**
     * Load progress reporting (for during load)
     */
    @NonNull
    public final Consumer<String> loadProgress;

    public final @NonNull FF0 launchConfigName;

    public HashMap<Integer, String> osSHESEDB;
    // scheduled tasks for when UI is around, not in UI because it may not init (ever, even!)
    public HashSet<Runnable> uiPendingRunnables = new HashSet<Runnable>();

    // this inits SDB's initial schemas, etc.
    public final SDBHelpers sdbHelpers;

    // these init during UI init!
    public AppUI ui;
    public AppNewProject np;

    // The global context-independent stuffRenderer. *Only use outside of maps.*
    public StuffRenderer stuffRendererIndependent;

    // State for in-system copy/paste
    public RORIO theClipboard = null;
    public final Runnable applyConfigChange;

    // configuration
    public final boolean deletionButtonsNeedConfirmation;

    // ID changer entries
    public final LinkedList<IDChangerEntry> idc = new LinkedList<>();

    public final CMDBDB cmdbs;

    /**
     * Command tags.
     */
    public final HashMap<String, CommandTag> commandTags = new HashMap<>();

    /**
     * Main list of command classifiers. For use by UICommandClassifierSet.
     */
    public final LinkedList<ICommandClassifier> cmdClassifiers = new LinkedList<>();

    /**
     * Main list of text analyzers. For use by all sorts of stuff.
     */
    public final LinkedList<ITextAnalyzer> textAnalyzers = new LinkedList<>();

    /**
     * Operators.
     */
    public final SchemaOp.OpNamespace operators;

    /**
     * Operators by their invoke contexts.
     */
    public final SchemaOp.SiteNamespace opSites;

    /**
     * 'Copy' and 'Paste' are actually operators.
     * This is particularly relevant for how the array selection logic interplays with commands these days.
     */
    public SchemaOp opCopy, opPaste;

    /**
     * Clipboard context in UTF-8
     */
    public final DMContext ctxClipboardUTF8Encoding = new DMContext(DMChangeTracker.Null.CLIPBOARD, StandardCharsets.UTF_8);

    /**
     * Disposable context in UTF-8
     */
    public final DMContext ctxDisposableUTF8Encoding = new DMContext(DMChangeTracker.Null.DISPOSABLE, StandardCharsets.UTF_8);

    /**
     * Clipboard context in app encoding
     */
    public final DMContext ctxClipboardAppEncoding;

    /**
     * Workspace context in app encoding
     */
    public final DMContext ctxWorkspaceAppEncoding;

    /**
     * Disposable context in app encoding
     */
    public final DMContext ctxDisposableAppEncoding;

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public App(InterlaunchGlobals ilg, @NonNull Charset charset, @NonNull EngineDef gp, @NonNull FSBackend rp, @Nullable FSBackend sip, Consumer<String> loadProgress, @NonNull FF0 launchConfigName) {
        this.ilg = ilg;
        this.encoding = charset;
        a = ilg.a;
        c = ilg.c;
        applyConfigChange = () -> {
            c.applyUIGlobals();
        };
        f = c.f;
        t = ilg.t;
        this.engine = gp;

        ctxClipboardAppEncoding = new DMContext(DMChangeTracker.Null.CLIPBOARD, encoding);
        ctxWorkspaceAppEncoding = new DMContext(DMChangeTracker.Null.WORKSPACE, encoding);
        ctxDisposableAppEncoding = new DMContext(DMChangeTracker.Null.DISPOSABLE, encoding);

        gameRoot = new DodgyInputWorkaroundFSBackend(rp);
        if (sip != null) {
            gameResources = new UnionFSBackend(gameRoot, new DodgyInputWorkaroundFSBackend(sip));
        } else {
            gameResources = new UnionFSBackend(gameRoot);
        }
        this.loadProgress = loadProgress;
        imageIOFormats = ImageIOFormat.initializeFormats(t);

        // time machine should be before data, because data uses time machine for management
        timeMachine = new TimeMachine(this);

        // Y'know, the VM could really be pushed to AppCore, but hmm.
        // I will say, in R48, everything is dependent on everything else.
        vmCtx = new MVMEnvR48((str) -> {
            loadProgress.accept(t.g.loadingProgress.r(str));
        }, ilg.logTrIssues, ilg.c.language, ilg.strict);

        sdb = new SDB(this);

        // initialize everything else that needs initializing, starting with ObjectDB
        IObjectBackend backend = IObjectBackend.Factory.create(gameRoot, engine.odbBackend, engine.dataPath, engine.dataExt);
        odb = new ObjectDB(this, backend);

        PleaseFailBrutally.checkFailBrutallyAtAppInit();

        this.launchConfigName = launchConfigName;

        // -- OBJECT DATABASE READY --

        operators = new SchemaOp.OpNamespace(vmCtx);
        opSites = new SchemaOp.SiteNamespace(vmCtx);

        deletionButtonsNeedConfirmation = GaBIEn.singleWindowApp();

        setupAnalysersAndClassifiers();

        // Setup Schema services that don't work without access to UI because reasons
        sdbHelpers = new SDBHelpers(this);
        cmdbs = new CMDBDB(this);
        system = MapSystem.create(this, engine.mapSystem);

        // Alright, the various bits of SDB are completely present but not yet initialized.
        // Run VM code to fill them with data.
        MVMR48AppLibraries.add(vmCtx, this);
        vmCtx.include("vm/global", false);
        vmCtx.include("vm/app", false);
        vmCtx.include(engine.initDir + "init", false);

        // Operators have to be initialized after the schemas used for their configuration.
        BaseSchemaOps.defJavasideOperators(this);

        // -- VM HAS FULLY INITIALIZED SCHEMA DATABASE --

        // Final internal consistency checks and reading in dictionaries from target
        //  before starting the UI, which can cause external consistency checks
        //  (...and potentially cause havoc in the process)

        loadProgress.accept(t.g.loadingDCO);
        sdb.startupSanitizeDictionaries(); // in case an object using dictionaries has to be created to use dictionaries
        sdb.updateDictionaries(null);
        sdb.confirmAllExpectationsMet();
        cmdbs.confirmAllExpectationsMet();

        // Now that everything that could possibly reasonably create DynTrSlots has been initialized, now load the language file.
        vmCtx.include(engine.initDir + "lang/" + ilg.c.language + "/init", true);
    }

    private void setupAnalysersAndClassifiers() {
        // setup command classifiers
        cmdClassifiers.add(new ICommandClassifier.Immutable() {
            @Override
            public String getName(App app) {
                return ilg.t.u.ccAll;
            }
            @Override
            public boolean matches(RPGCommand target, @Nullable RORIO data) {
                return true;
            }
        });

        // setup text analyzers
        for (ITextAnalyzer ita : TextOperator.values())
            textAnalyzers.add(ita);
        textAnalyzers.add(ITextAnalyzer.CJK.I);
        textAnalyzers.add(ITextAnalyzer.NotLatin1.I);
        textAnalyzers.add(ITextAnalyzer.NotLatin1OrFullwidth.I);

        // mutable text analyzers to command classifier
        cmdClassifiers.add(new TextAnalyzerCommandClassifier(CompoundTextAnalyzer.I));

        // mirror immutable text analyzers to command classifiers (so USL can access them)
        for (ITextAnalyzer ita : textAnalyzers)
            if (ita instanceof ITextAnalyzer.Immutable)
                cmdClassifiers.add(new ImmutableTextAnalyzerCommandClassifier((ITextAnalyzer.Immutable) ita));
        cmdClassifiers.add(new ByCodeCommandClassifier());
    }

    /**
     * Sets the clipboard to a deep clone of a value.
     */
    public void setClipboardFrom(IRIO frame) {
        theClipboard = new IRIOGeneric(ctxClipboardAppEncoding).setDeepClone(frame);
    }

    @Override
    public DynTrBase dynTrBase(DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object text, boolean isNLS) {
        return vmCtx.dynTrBase(srcLoc, id, mode, text, isNLS);
    }

    public void performTranslatorDump(String fn) {
        vmCtx.dynTrDump(fn);
    }

    private @Nullable FF1 getNameDB(String name) {
        MVMSlot slot = vmCtx.getSlot(new DatumSymbol(TrNames.nameRoutine(name)));
        if (slot != null)
            return (FF1) slot.v;
        return null;
    }

    /**
     * Formats a RORIO using the given name routine (if it exists) and prefix mode.
     */
    public String format(RORIO rubyIO, String st, EnumSchemaElement.Prefix prefixEnums) {
        if (rubyIO == null)
            return "";
        if (st != null) {
            FF1 handler = getNameDB(st);
            if (handler != null) {
                return handler.r(rubyIO);
            } else if (sdb.hasSDBEntry(st)) {
                SchemaElement ise = sdb.getSDBEntry(st);
                return format(rubyIO, ise, prefixEnums);
            }
        }
        return format(rubyIO, (SchemaElement) null, prefixEnums);
    }

    /**
     * Formats a RORIO (with no enum prefix).
     */
    public String format(RORIO rubyIO) {
        return format(rubyIO, (SchemaElement) null, EnumSchemaElement.Prefix.NoPrefix);
    }

    /**
     * Formats a RORIO assuming the given schema element (if it exists) and prefix mode.
     */
    public String format(RORIO rubyIO, SchemaElement ise, EnumSchemaElement.Prefix prefixEnums) {
        // Basically, Class. overrides go first, then everything else comes after.
        if (rubyIO.getType() == 'o') {
            FF1 handler = getNameDB("Class." + rubyIO.getSymbol());
            if (handler != null)
                return handler.r(rubyIO);
        }
        String r = null;
        if (ise != null) {
            ise = AggregateSchemaElement.extractField(ise, rubyIO);
            if (ise instanceof EnumSchemaElement)
                r = ((EnumSchemaElement) ise).viewValue(rubyIO, prefixEnums);
        }
        if (r == null)
            r = rubyIO.toString();
        return r;
    }

    /**
     * Finishes initialization on main thread just before ticking begins.
     */
    public void finishInitOnMainThread() {
        ui.finishInitialization();
    }

    public void tick(double dT) {
        ui.tick(dT);
    }

    public void shutdown() {
        if (ui != null) {
            if (ui.mapContext != null)
                ui.mapContext.freeOsbResources();
            ui.mapContext = null;
        }
        GaBIEn.hintFlushAllTheCaches();
    }

    public void reportNonCriticalErrorToUser(String r, Throwable ioe) {
        ui.launchDialog(r, ioe);
    }

    public LinkedList<String> getAllObjects() {
        // anything loaded gets added (this allows some bypass of the mechanism)
        HashSet<String> mainSet = new HashSet<String>(odb.objectMap.keySet());
        for (ObjectInfo oi : sdb.listFileDefs())
            mainSet.add(oi.idName);
        for (ObjectInfo dobj : system.getDynamicObjects())
            mainSet.add(dobj.idName);
        return new LinkedList<String>(mainSet);
    }

    /**
     * Attempts to ascertain all known objects
     */
    public LinkedList<ObjectInfo> getObjectInfos() {
        LinkedList<ObjectInfo> oi = sdb.listFileDefs();
        for (ObjectInfo dobj : system.getDynamicObjects())
            oi.add(dobj);
        return oi;
    }

    /**
     * Gets a specific object info.
     */
    @Nullable
    public ObjectInfo getObjectInfo(String text) {
        for (ObjectInfo oi : getObjectInfos())
            if (oi.idName.equals(text))
                return oi;
        return null;
    }

    // -- TimeMachine.Host --

    @Override
    public void timeMachineHostOnTimeTravel() {
        sdb.kickAllDictionariesForMapChange();
    }

    // -- ObjectDB.Host --

    @Override
    public Charset odbHostGetEncoding() {
        return encoding;
    }

    @Override
    public TimeMachine odbHostGetTimeMachine() {
        return timeMachine;
    }

    @Override
    public void odbHostObjectLoadMsg(String objectId) {
        loadProgress.accept(t.u.odb_loadObj.r(objectId));
    }

    @Override
    public void odbHostReportSaveError(String id, Exception ioe) {
        reportNonCriticalErrorToUser(t.u.odb_saveErr.r(id), ioe);        
    }

    @Override
    public SchemaElementIOP odbHostGetOpaqueSE() {
        return sdb.getSDBEntry("OPAQUE");
    }

    @Override
    public SchemaElementIOP odbHostMapObjectIDToSchema(String id) {
        return system.mapObjectIDToSchema(id);
    }

    // Svc

    public static class Svc {
        public final @NonNull App app;
        /**
         * This is a special exception to the usual style rules.
         */
        public final @NonNull TrRoot T;
        public Svc(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static class Prx extends UIProxy {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Prx(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static abstract class Pan extends UIPanel {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Pan(@NonNull App app) {
            this.app = app;
            T = app.t;
        }
    }

    public static abstract class Elm extends UIElement {
        public final @NonNull App app;
        public final @NonNull TrRoot T;
        public Elm(@NonNull App app) {
            this.app = app;
            T = app.t;
        }

        public Elm(@NonNull App app, int i, int j) {
            super(i, j);
            this.app = app;
            T = app.t;
        }
    }
}
