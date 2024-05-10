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

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.ui.UIElement;
import gabien.ui.UIElement.UIPanel;
import gabien.ui.UIElement.UIProxy;
import gabien.uslx.vfs.FSBackend;
import r48.app.AppCore;
import r48.app.AppNewProject;
import r48.app.AppUI;
import r48.app.EngineDef;
import r48.app.IAppAsSeenByLauncher;
import r48.app.InterlaunchGlobals;
import r48.dbs.CMDBDB;
import r48.dbs.RPGCommand;
import r48.dbs.SDBHelpers;
import r48.io.data.DMContext;
import r48.io.data.DMChangeTracker;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.map.StuffRenderer;
import r48.map.systems.MapSystem;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMSlot;
import r48.minivm.fn.MVMR48AppLibraries;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
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
import r48.tr.TrPage.FF1;
import r48.tr.pages.TrRoot;

/**
 * An attempt to move as much as possible out of static variables.
 * The distinction here is that when possible:
 *  AppCore would hypothetically work without a UI
 *  App won't
 * Created 26th February, 2023
 */
public final class App extends AppCore implements IAppAsSeenByLauncher, IDynTrProxy {
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
    public final Runnable applyConfigChange = () -> {
        c.applyUIGlobals();
    };

    // configuration
    public final boolean deletionButtonsNeedConfirmation;

    // ID changer entries
    public final LinkedList<IDChangerEntry> idc = new LinkedList<>();

    // VM context
    public final MVMEnvR48 vmCtx;

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
     * Clipboard context in app encoding
     */
    public final DMContext ctxClipboardAppEncoding = new DMContext(DMChangeTracker.Null.CLIPBOARD, encoding);

    /**
     * Clipboard context in UTF-8
     */
    public final DMContext ctxClipboardUTF8Encoding = new DMContext(DMChangeTracker.Null.CLIPBOARD, StandardCharsets.UTF_8);

    /**
     * Workspace context in app encoding
     */
    public final DMContext ctxWorkspaceAppEncoding = new DMContext(DMChangeTracker.Null.WORKSPACE, encoding);

    /**
     * Disposable context in UTF-8
     */
    public final DMContext ctxDisposableUTF8Encoding = new DMContext(DMChangeTracker.Null.DISPOSABLE, StandardCharsets.UTF_8);

    /**
     * Disposable context in app encoding
     */
    public final DMContext ctxDisposableAppEncoding = new DMContext(DMChangeTracker.Null.DISPOSABLE, encoding);

    /**
     * Delme context in UTF-8
     */
    public final DMContext ctxDelmeAppEncoding = new DMContext(DMChangeTracker.Null.DELETE_ME, encoding);

    /**
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public App(InterlaunchGlobals ilg, @NonNull Charset charset, @NonNull EngineDef gp, @NonNull FSBackend rp, @Nullable FSBackend sip, Consumer<String> loadProgress) {
        super(ilg, charset, gp, rp, sip, loadProgress);

        // -- OBJECT DATABASE READY --

        deletionButtonsNeedConfirmation = GaBIEn.singleWindowApp();

        setupAnalysersAndClassifiers();

        // Setup Schema services that don't work without access to UI because reasons
        sdbHelpers = new SDBHelpers(this);
        cmdbs = new CMDBDB(this);
        system = MapSystem.create(this, engine.mapSystem);

        // Y'know, the VM could really be pushed to AppCore, but hmm.
        // I will say, in R48, everything is dependent on everything else.
        vmCtx = new MVMEnvR48((str) -> {
            loadProgress.accept(t.g.loadingProgress.r(str));
        }, ilg.logTrIssues, ilg.c.language, ilg.strict);

        // Alright, the various bits of SDB are completely present but not yet initialized.
        // Run VM code to fill them with data.
        MVMR48AppLibraries.add(vmCtx, this);
        vmCtx.include("vm/global", false);
        vmCtx.include("vm/app", false);
        vmCtx.include(engine.initDir + "init", false);

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

    @Override
    public void reportNonCriticalErrorToUser(String r, Throwable ioe) {
        ui.launchDialog(r, ioe);
    }

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
