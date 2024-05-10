/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.integers;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOTypedMask;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.ui.UIMenuButton;

/**
 * An integer with several "namespaces".
 * Created 10th May, 2024.
 */
public class NamespacedIntegerSchemaElement extends SchemaElement {
    private final Namespace[] namespaces;

    public NamespacedIntegerSchemaElement(App app, Namespace... namespaces) {
        super(app);
        this.namespaces = namespaces;
        // sanity check
        namespaceOf(Long.MIN_VALUE);
        namespaceOf(0);
        namespaceOf(Long.MAX_VALUE);
    }

    public @Nullable Namespace namespaceOf(long v) {
        for (Namespace n : namespaces)
            if (n.contains(v))
                return n;
        throw new RuntimeException("It sure is important that you have an Unknown namespace, huh!");
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        Namespace ns = namespaceOf(target.getFX());
        LinkedList<UIPopupMenu.Entry> mapped = new LinkedList<>();
        for (final Namespace ns2 : namespaces) {
            mapped.add(new UIPopupMenu.Entry(ns2.name.r(), () -> {
                ns2.editor.modifyVal(new NamespacingMask(target, ns2.base), path, true);
                path.changeOccurred(false);
            }));
        }
        UIElement nsPanel = new UIMenuButton(app, ns.name.r(), app.f.schemaFieldTH, launcher.getValidity(), mapped);
        UIElement valuePanel = ns.editor.buildHoldingEditor(new NamespacingMask(target, ns.base), launcher, path);
        if (ns.explain != null) {
            UIElement explainPanel = new UILabel(ns.explain.r(), app.f.schemaFieldTH);
            return new UISplitterLayout(new UISplitterLayout(nsPanel, valuePanel, false, 0), explainPanel, true, 0);
        } else {
            return new UISplitterLayout(nsPanel, valuePanel, false, 0);
        }
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        Namespace ns;
        if (setDefault || target.getType() != 'i') {
            setDefault = true;
            ns = namespaces[0];
        } else {
            ns = namespaceOf(target.getFX());
        }
        ns.editor.modifyVal(new NamespacingMask(target, ns.base), path, setDefault);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        Namespace ns = namespaceOf(target.getFX());
        ns.editor.visit(new NamespacingMask(target, ns.base), path, v, detailedPaths);
    }

    public static final class Namespace {
        public final @NonNull FF0 name;
        public final @Nullable FF0 explain;
        public final long firstValue;
        public final long lastValue;
        public final long base;
        public SchemaElement editor;
        public Namespace(@NonNull FF0 name, @Nullable FF0 explain, long firstValue, long lastValue, long base, SchemaElement editor) {
            this.name = name;
            this.explain = explain;
            this.firstValue = firstValue;
            this.lastValue = lastValue;
            this.base = base;
            this.editor = editor;
        }
        public boolean contains(long v) {
            return v >= firstValue && v <= lastValue;
        }
    }

    /**
     * Wraps an IRIO to offset integer values.
     * First 'Pointer IRIO' ever created!
     */
    public static final class NamespacingMask extends IRIOTypedMask {
        public final IRIO target;
        public final long base;
        public NamespacingMask(IRIO target, long base) {
            super(target.context);
            this.target = target;
            this.base = base;
        }

        @Override
        public IRIO addIVar(String sym) {
            return null;
        }
        @Override
        public IRIO getIVar(String sym) {
            return null;
        }
        @Override
        public int getType() {
            return 'i';
        }
        @Override
        public String[] getIVars() {
            return new String[0];
        }
        @Override
        public IRIO setFX(long fx) {
            target.setFX(fx + base);
            return this;
        }
        @Override
        public long getFX() {
            return target.getFX() - base;
        }
    }
}
