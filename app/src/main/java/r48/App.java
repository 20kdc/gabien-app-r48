/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import java.nio.charset.Charset;
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
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;
import r48.map.StuffRenderer;
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
 * Created 26th February, 2023
 */
public final class App extends AppCore implements IAppAsSeenByLauncher, IDynTrProxy {
    public HashMap<Integer, String> osSHESEDB;
    // scheduled tasks for when UI is around, not in UI because it may not init (ever, even!)
    public HashSet<Runnable> uiPendingRunnables = new HashSet<Runnable>();
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

    // ID changer entries
    public final LinkedList<IDChangerEntry> idc = new LinkedList<>();

    // VM context
    public final MVMEnvR48 vmCtx;

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
     * Initialize App.
     * Warning: Occurs off main thread.
     */
    public App(InterlaunchGlobals ilg, @NonNull Charset charset, @NonNull EngineDef gp, @NonNull FSBackend rp, @Nullable FSBackend sip, Consumer<String> loadProgress) {
        super(ilg, charset, gp, rp, sip, loadProgress);
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
        // do VM stuff
        vmCtx = new MVMEnvR48((str) -> {
            loadProgress.accept(t.g.loadingProgress.r(str));
        }, ilg.logTrIssues, ilg.c.language);
        // needs to init after vmCtx to install system name routines
        MVMR48AppLibraries.add(vmCtx, this);
        vmCtx.include("vm/global", false);
        vmCtx.include("vm/app", false);
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
