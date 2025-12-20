/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.elements.UIButton;
import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.specialized.TempDialogSchemaChoice;

/**
 * Used to make the Schema interface slightly saner to use
 * Created on 12/29/16.
 */
public interface ISchemaHost {
    /**
     * Navigates to an object, putting it on the history stack.
     */
    void pushObject(SchemaPath nextObject);

    /**
     * Goes backwards through the stack, navigating to the previous object.
     * If canClose is true and the stack is empty, the host may be closed.
     */
    void popObject(boolean canClose);

    default void pushPathTree(SchemaPath nextObject) {
        // "Decompile" the path into a usable forward/back tree.
        LinkedList<SchemaPath> rv = new LinkedList<>();
        while (nextObject != null) {
            if (nextObject.editor != null)
                rv.addFirst(nextObject);
            nextObject = nextObject.parent;
        }
        for (SchemaPath sp : rv)
            pushObject(sp);
    }

    void launchOther(UIElement uiTest);

    /**
     * Gets the 'dynamic context'.
     * This is essentially just a wrapper for UIMapView that makes it clear when this is being passed around.
     * It also retrieves the StuffRenderer.
     */
    @NonNull SchemaDynamicContext getContext();

    ISchemaHost newBlank();

    SchemaPath getCurrentObject();

    default <T> EmbedDataSlot<T> embedSlot(IRIO target, EmbedDataKey<T> prop, T def) {
        return embedSlot(getCurrentObject(), target, prop, def);
    }

    <T> EmbedDataSlot<T> embedSlot(SchemaPath locale, IRIO target, EmbedDataKey<T> prop, T def);

    default IEmbedDataContext embedContext(IRIO target) {
        return embedContext(getCurrentObject(), target);
    }

    default IEmbedDataContext embedContext(SchemaPath locale, IRIO target) {
        return new IEmbedDataContext() {
            @Override
            public <T> EmbedDataSlot<T> embedSlot(EmbedDataKey<T> prop, T def) {
                return ISchemaHost.this.embedSlot(locale, target, prop, def);
            }
        };
    }

    Supplier<Boolean> getValidity();

    /**
     * Supplies context for operators.
     * The operator context is cleared before each buildHoldingEditor.
     */
    default void addOperatorContext(IRIO target, String key, DMKey value) {
    }

    /**
     * Copies the operator context for modification and further use.
     */
    HashMap<String, DMKey> copyOperatorContext();

    // Yet another way to get an App to avoid pipelining
    App getApp();

    /**
     * Usually creates a TempDialogSchemaChoice.
     * However, in some circumstances, it will instead create a menu.
     * The theoretical ideal here is that this 'replaces' TempDialogSchemaChoice API-wise.
     */
    default void launchDialogOrMenu(UIButton<?> button, IRIO target, SchemaPath path, Function<Runnable, UIElement> dialogMaker) {
        TempDialogSchemaChoice temp = new TempDialogSchemaChoice(getApp(), null, path);
        temp.heldDialog = dialogMaker.apply(temp::pleasePopObject);
        pushObject(path.newWindow(temp, target));
    }
}
