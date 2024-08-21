/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import java.util.LinkedList;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import r48.App;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;

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
     * The StuffRenderer applicable to this window.
     */
    @NonNull StuffRenderer getContextRenderer();

    ISchemaHost newBlank();

    SchemaPath getCurrentObject();

    String getContextGUM();

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

    // Yet another way to get an App to avoid pipelining
    App getApp();
}
