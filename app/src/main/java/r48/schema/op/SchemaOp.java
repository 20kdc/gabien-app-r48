/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.op;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSymbol;
import gabien.ui.UIElement;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UIPublicPanel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.UITest;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMChangeTracker;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.map.UIMapView;
import r48.minivm.MVMEnv;
import r48.minivm.aux.MVMNamespace;
import r48.schema.SchemaElement;
import r48.schema.displays.LabelSchemaElement;
import r48.schema.util.SchemaPath;
import r48.schema.util.UISchemaHostWidget;
import r48.toolsets.utils.UIIDChanger;
import r48.tr.TrPage.FF0;
import r48.ui.UIMenuButton;

/**
 * Somewhat of a compromise between different design philosophies.
 * R48 operators are found by the UI and then attached into the operators panel.
 * Created 19th December, 2025.
 */
public abstract class SchemaOp extends App.Svc {
    public static final DatumSymbol BASE_SYSCORE = new DatumSymbol("r48core_");

    // See ISchemaHost.supplyOperatorContext
    /**
     * Parameter from UI: if array selection is non-empty, start index. 
     */
    public static final String CTXPARAM_ARRAYSTART = "@ctx_array_start";
    /**
     * Parameter from UI: if array selection is non-empty, end (exclusive) index.
     */
    public static final String CTXPARAM_ARRAYEND = "@ctx_array_end";

    /**
     * Operator ID. This may be used in future to allow triggering the operator from D/MVM.
     */
    public final @NonNull DatumSymbol id;

    /**
     * Operator name.
     * This is partially a code migration thing.
     * Most operators will probably be sourcing their names from a specific TrPage.
     */
    public final @NonNull FF0 name;

    /**
     * Sort key.
     */
    public final int sort;

    /**
     * Configuration schema.
     */
    public final @Nullable SchemaElement config;

    /**
     * Save/Inspect etc.
     */
    public static final int SORT_SYSCORE = 0x00000000;
    /**
     * LIDC etc.
     */
    public static final int SORT_VISITOR = 0x01000000;
    /**
     * Text tools and such.
     */
    public static final int SORT_RMTOOLS = 0x02000000;

    public SchemaOp(App app, DatumSymbol id, FF0 name, int sort, SchemaOpSite... sites) {
        super(app);
        this.id = id;
        this.name = name;
        this.sort = sort;
        String sdbLookup = "R48::OpCfg::" + id.id;
        this.config = app.sdb.hasSDBEntry(sdbLookup) ? app.sdb.getSDBEntry(sdbLookup) : null;
        app.operators.put(id, this);
        for (SchemaOpSite site : sites)
            site.operators.add(this);
    }

    public SchemaOp(App app, DatumSymbol base, String sfx, FF0 name, int sort, SchemaOpSite... sites) {
        this(app, new DatumSymbol(base.id + sfx), name, sort, sites);
    }

    /**
     * If the operator should be displayed at all.
     * Note that this is not a total ban on invoking the operator.
     * The idea is that this allows the UI to target operators to where they're desired.
     */
    public boolean shouldDisplay(SchemaPath path, Function<String, DMKey> context) {
        return true;
    }

    /**
     * Invokes the operator.
     * Operators may only be invoked at displayed paths.
     */
    public abstract void invoke(SchemaPath path, RORIO parameters);

    /**
     * Helps with defining the system core operators.
     */
    private static void sysOperator(App app, String id, FF0 name, Consumer<SchemaPath> handler, int idx, SchemaOpSite... sites) {
        new SchemaOp(app, BASE_SYSCORE, id, name, SORT_SYSCORE + (idx * 0x10000), sites) {
            @Override
            public void invoke(SchemaPath path, RORIO parameters) {
                handler.accept(path);
            }
        };
    }

    /**
     * This is the central list of all built-in (Java-side) operators.
     */
    public static void defJavasideOperators(App app) {
        sysOperator(app, "save", () -> app.t.g.wordSave, (innerElem) -> {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
            // root.editor.modifyVal(root.targetElement, root, false);
            root.root.ensureSaved();
        }, 0, app.opSites.SCHEMA_HEADER);
        sysOperator(app, "inspect", () -> app.t.u.shInspect, (innerElem) -> {
            app.ui.wm.createWindow(new UITest(app, innerElem.targetElement, innerElem.root));
        }, 1, app.opSites.SCHEMA_HEADER);
        sysOperator(app, "localidchanger", () -> app.t.u.shLIDC, (innerElem) -> {
            // innerElem.editor and innerElem.targetElement must exist because SchemaHostImpl uses them.
            app.ui.wm.createWindow(new UIIDChanger(app, innerElem));
        }, 2, app.opSites.SCHEMA_HEADER);

        new SchemaOp(app, BASE_SYSCORE, "test_operator", () -> "TEST OPERATOR", SORT_SYSCORE + (2 * 0x10000), app.opSites.SCHEMA_HEADER, app.opSites.ARRAY_SEL) {
            @Override
            public void invoke(SchemaPath path, RORIO parameters) {
            }
        };
    }

    /**
     * Creates an operator config root handle.
     */
    public final @NonNull ObjectRootHandle createOperatorConfig() {
        SchemaElement se = config;
        if (se == null)
            se = new LabelSchemaElement(app, () -> "UNCONFIGURABLE OPERATOR - YOU SHOULD NOT SEE THIS");
        IRIOGeneric ig = new IRIOGeneric(new DMContext(DMChangeTracker.Null.OPERATOR_CONFIG, StandardCharsets.UTF_8));
        SchemaPath.setDefaultValue(ig, se, null);
        ObjectRootHandle.Isolated root = new ObjectRootHandle.Isolated(se, ig, "operator-config");
        root.getObject().setObject("R48::OpCfg::" + id.id);
        se.modifyVal(root.getObject(), new SchemaPath(se, root), true);
        return root;
    }

    /**
     * Builds an operator menu.
     */
    public static UIElement createOperatorMenu(App app, SchemaPath innerElem, SchemaOpSite site, Supplier<Boolean> validity, Map<String, DMKey> operatorContext, UIMapView rendererSource) {
        LinkedList<UIPopupMenu.Entry> entries = new LinkedList<>();
        LinkedList<SchemaOp> ll = new LinkedList<>();
        Function<String, DMKey> contextSupplier = operatorContext::get;
        for (SchemaOp op : site.operators)
            if (op.shouldDisplay(innerElem, contextSupplier))
                ll.add(op);
        ll.sort((arg0, arg1) -> {
            if (arg0.sort < arg1.sort)
                return -1;
            if (arg0.sort > arg1.sort)
                return 1;
            return arg0.id.compareTo(arg1.id);
        });
        for (final SchemaOp op : ll) {
            entries.add(new UIPopupMenu.Entry(op.name.r(), (button) -> {
                ObjectRootHandle cfg = op.createOperatorConfig();
                // merge in context
                for (Map.Entry<String, DMKey> context : operatorContext.entrySet())
                    cfg.getObject().addIVar(context.getKey()).setDeepClone(context.getValue());
                // and now for the rest...
                if (op.config != null) {
                    UISchemaHostWidget wdg = new UISchemaHostWidget(app, rendererSource);
                    wdg.pushObject(new SchemaPath(cfg));
                    AtomicBoolean invalidator = new AtomicBoolean(false);
                    UITextButton confirmButton = new UITextButton(app.t.g.bConfirm, app.f.schemaFieldTH, () -> {
                        if (validity.get() && !invalidator.get())
                            op.invoke(innerElem, cfg.getObject());
                        invalidator.set(true);
                    });  
                    UISplitterLayout confirmBar = new UISplitterLayout(new UIPublicPanel(0, 0), confirmButton, false, 1);
                    UISplitterLayout wdgAndConfirm = new UISplitterLayout(wdg, confirmBar, true, 0) {
                        @Override
                        public boolean requestsUnparenting() {
                            // the operator should auto-cancel if anything goes wrong
                            return invalidator.get() || !validity.get();
                        }
                    };  
                    UIMenuButton.corePostHoc(app, button, wdgAndConfirm);
                } else {
                    op.invoke(innerElem, cfg.getObject());
                }
            }));
        }
        return UIMenuButton.coreMenuGen(app, validity, entries);
    }

    public final static class SiteNamespace extends MVMNamespace<SchemaOpSite> {
        public final SchemaOpSite SCHEMA_HEADER;
        public final SchemaOpSite ARRAY_SEL;

        public SiteNamespace(MVMEnv env) {
            super("SchemaOpSite.", "sop-site", env, "SchemaOp.Site", SchemaOpSite.class);
            add("SCHEMA_HEADER", SCHEMA_HEADER = new SchemaOpSite());
            add("ARRAY_SEL", ARRAY_SEL = new SchemaOpSite());
        }
        @Override
        protected boolean supportsNew() {
            return true;
        }
        @Override
        protected SchemaOpSite createNew(String name) {
            return new SchemaOpSite();
        }
    }
}
