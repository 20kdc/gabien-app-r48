/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import java.util.LinkedList;
import java.util.function.Supplier;

import gabien.ui.UIElement;
import r48.App;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.schema.SchemaElement;

/**
 * Used to make the Schema interface slightly saner to use
 * Created on 12/29/16.
 */
public interface ISchemaHost {
    void pushObject(SchemaPath nextObject);
    void popObject();

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

    // The StuffRenderer applicable to this window.
    StuffRenderer getContextRenderer();

    ISchemaHost newBlank();

    boolean isActive();

    SchemaPath getCurrentObject();

    String getContextGUM();

    default double getEmbedDouble(SchemaElement source, IRIO target, String prop) {
        return (Double) getEmbedObject(source, target, prop, 0.0d); 
    }

    default void setEmbedDouble(SchemaElement source, IRIO target, String prop, double dbl) {
        setEmbedObject(source, target, prop, dbl);
    }

    default Object getEmbedObject(SchemaElement source, IRIO target, String prop) {
        return getEmbedObject(source, target, prop, null);
    }
    
    Object getEmbedObject(SchemaElement source, IRIO target, String prop, Object def);

    default Object getEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop) {
        return getEmbedObject(locale, source, target, prop, null);
    }

    Object getEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object def);

    void setEmbedObject(SchemaElement source, IRIO target, String prop, Object dbl);

    void setEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object dbl);

    Supplier<Boolean> getValidity();

    // Used to shutdown all schema hosts during a revert.
    // No-op if the host isn't active.
    void shutdown();

    // Yet another way to get an App to avoid pipelining
    App getApp();
}
