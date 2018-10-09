/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.util;

import gabien.ui.UIElement;
import r48.RubyIO;
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
    double getEmbedDouble(SchemaElement source, RubyIO target, String prop);

    void setEmbedDouble(SchemaElement source, RubyIO target, String prop, double dbl);

    Object getEmbedObject(SchemaElement source, RubyIO target, String prop);

    void setEmbedObject(SchemaElement source, RubyIO target, String prop, Object dbl);
}
