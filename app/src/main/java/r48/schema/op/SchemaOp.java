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
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.undoredo.DMChangeTracker;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.aux.MVMNamespace;
import r48.schema.SchemaElement;
import r48.schema.displays.LabelSchemaElement;
import r48.schema.util.SchemaDynamicContext;
import r48.schema.util.SchemaPath;
import r48.schema.util.UISchemaHostWidget;
import r48.ui.UIMenuButton;

/**
 * Somewhat of a compromise between different design philosophies.
 * R48 operators are found by the UI and then attached into the operators panel.
 * Created 19th December, 2025.
 */
public abstract class SchemaOp extends App.Svc {

    /**
     * Operator ID. This may be used in future to allow triggering the operator from D/MVM.
     */
    public final @NonNull DatumSymbol id;

    /**
     * Sort key.
     */
    public final int sort;

    /**
     * Configuration schema.
     */
    public final @Nullable SchemaElement config;

    public SchemaOp(App app, DatumSymbol id, int sort, SchemaOpSite... sites) {
        super(app);
        this.id = id;
        this.sort = sort;
        String sdbLookup = "R48::OpCfg::" + id.id;
        this.config = app.sdb.hasSDBEntry(sdbLookup) ? app.sdb.getSDBEntry(sdbLookup) : null;
        app.operators.add(id.id, this);
        for (SchemaOpSite site : sites)
            site.operators.add(this);
    }

    public SchemaOp(App app, DatumSymbol base, String sfx, int sort, SchemaOpSite... sites) {
        this(app, new DatumSymbol(base.id + sfx), sort, sites);
    }

    /**
     * If the operator should be displayed at all, and what it should display.
     * Note that this is not a total ban on invoking the operator.
     * The idea is that this allows the UI to target operators to where they're desired.
     */
    public String shouldDisplay(ExpandedCtx context) {
        return null;
    }

    /**
     * Invokes the operator.
     * Operators may only be invoked at displayed paths.
     * Returns a message (or null for none).
     */
    public abstract @Nullable String invoke(ExpandedCtx context);

    /**
     * Invokes the operator, guarding against errors by showing an error message.
     * Also shows regular messages.
     */
    public void invokeUI(ExpandedCtx context) {
        try {
            String v = invoke(context);
            if (v != null)
                app.ui.launchDialog(v);
        } catch (Exception ex) {
            ex.printStackTrace();
            app.ui.launchDialog(app.t.s.op_error.r(id), ex);
        }
    }

    /**
     * See the other definition, but this one runs the full expansion from no configuration first.
     */
    public void invokeUI(SchemaPath path, Map<String, DMKey> context) {
        invokeUI(new SchemaOp.ExpandedCtx(path, createOperatorConfig(context).getObject()));
    }

    /**
     * Creates an operator config root handle.
     */
    public final @NonNull ObjectRootHandle createOperatorConfig(Map<String, DMKey> operatorContext) {
        SchemaElement se = config;
        if (se == null)
            se = new LabelSchemaElement(app, () -> "UNCONFIGURABLE OPERATOR - YOU SHOULD NOT SEE THIS");
        IRIOGeneric ig = new IRIOGeneric(new DMContext(DMChangeTracker.Null.OPERATOR_CONFIG, StandardCharsets.UTF_8));
        SchemaPath.setDefaultValue(ig, se, null);
        ObjectRootHandle.Isolated root = new ObjectRootHandle.Isolated(se, ig, "operator-config");
        root.getObject().setObject("R48::OpCfg::" + id.id);
        se.modifyVal(root.getObject(), new SchemaPath(se, root), true);
        // merge in context
        for (Map.Entry<String, DMKey> context : operatorContext.entrySet())
            root.getObject().addIVar(context.getKey()).setDeepClone(context.getValue());
        return root;
    }

    /**
     * Builds an operator menu.
     */
    public static void createOperatorMenuEntries(LinkedList<UIPopupMenu.Entry> entries, SchemaPath innerElem, SchemaOpSite site, Supplier<Boolean> validity, Map<String, DMKey> operatorContext, SchemaDynamicContext rendererSource) {
        App app = rendererSource.app;
        LinkedList<SchemaOp> ll = new LinkedList<>();
        ExpandedCtx ctx1 = new ExpandedCtx(innerElem, operatorContext::get);
        for (SchemaOp op : site.operators)
            if (op.shouldDisplay(ctx1) != null)
                ll.add(op);
        ll.sort((arg0, arg1) -> {
            if (arg0.sort < arg1.sort)
                return -1;
            if (arg0.sort > arg1.sort)
                return 1;
            return arg0.id.compareTo(arg1.id);
        });
        for (final SchemaOp op : ll) {
            String info = op.shouldDisplay(ctx1);
            if (info == null)
                info = "NON-DETERMINISTIC OPERATOR YOU SHOULD NOT SEE THIS";
            entries.add(new UIPopupMenu.Entry(info, (button) -> {
                ObjectRootHandle cfg = op.createOperatorConfig(operatorContext);
                // and now for the rest...
                if (op.config != null) {
                    UISchemaHostWidget wdg = new UISchemaHostWidget(app, rendererSource);
                    wdg.pushObject(new SchemaPath(cfg));
                    AtomicBoolean invalidator = new AtomicBoolean(false);
                    UITextButton confirmButton = new UITextButton(app.t.g.bConfirm, app.f.schemaFieldTH, () -> {
                        if (validity.get() && !invalidator.get())
                            op.invokeUI(new ExpandedCtx(innerElem, cfg.getObject()));
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
                    op.invokeUI(new ExpandedCtx(innerElem, cfg.getObject()));
                }
            }));
        }
    }

    /**
     * Builds an operator menu.
     */
    public static UIElement createOperatorMenu(SchemaPath innerElem, SchemaOpSite site, Supplier<Boolean> validity, Map<String, DMKey> operatorContext, SchemaDynamicContext rendererSource) {
        App app = rendererSource.app;
        LinkedList<UIPopupMenu.Entry> entries = new LinkedList<>();
        createOperatorMenuEntries(entries, innerElem, site, validity, operatorContext, rendererSource);
        return UIMenuButton.coreMenuGen(app, validity, entries);
    }

    /**
     * Expanded context. Includes extra things we don't want to try and reparse over and over.
     */
    public final static class ExpandedCtx implements Function<String, DMKey> {
        public final SchemaPath path;
        public final Function<String, DMKey> context;
        public final CommandListSelection commandList;

        public ExpandedCtx(SchemaPath path, Function<String, DMKey> ctx) {
            this.path = path;
            this.context = ctx;
            this.commandList = CommandListSelection.extractSelection(path, ctx);
        }

        public ExpandedCtx(SchemaPath path, RORIO ctx) {
            this(path, (s) -> {
                RORIO tmp = ctx.getIVar(s);
                if (tmp == null)
                    return null;
                return tmp.asKey();
            });
        }

        @Override
        public DMKey apply(String t) {
            return context.apply(t);
        }
    }

    public final static class SiteNamespace extends MVMNamespace<SchemaOpSite> {
        public final SchemaOpSite SCHEMA_HEADER;
        public final SchemaOpSite ARRAY_SEL;

        public SiteNamespace(MVMEnv env) {
            super(null, "sop-site", env, "schema-site", SchemaOpSite.class);
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

    public final static class OpNamespace extends MVMNamespace<SchemaOp> {
        public OpNamespace(MVMEnv env) {
            super(null, "sop", env, "schema-op", MVMEnvR48.SCHEMAOP_TYPE);
        }
        @Override
        protected boolean selfInstalling() {
            return true;
        }
    }
}
