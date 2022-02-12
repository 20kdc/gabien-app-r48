/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.util;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
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

    void launchOther(UIElement uiTest);

    // The StuffRenderer applicable to this window.
    StuffRenderer getContextRenderer();

    ISchemaHost newBlank();

    boolean isActive();

    SchemaPath getCurrentObject();

    String getContextGUM();

    // Prepare for trouble, and make it a double.
    double getEmbedDouble(SchemaElement source, IRIO target, String prop);

    void setEmbedDouble(SchemaElement source, IRIO target, String prop, double dbl);

    Object getEmbedObject(SchemaElement source, IRIO target, String prop);

    Object getEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop);

    void setEmbedObject(SchemaElement source, IRIO target, String prop, Object dbl);

    void setEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object dbl);

    ISupplier<Boolean> getValidity();

    // Used to shutdown all schema hosts during a revert.
    // No-op if the host isn't active.
    void shutdown();
}
