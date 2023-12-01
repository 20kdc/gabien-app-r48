/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.ui.*;
import r48.App;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.schema.SchemaElement;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted from SchemaHostImpl, 17th July, 2023.
 */
public abstract class SchemaHostBase extends App.Pan implements ISchemaHost {
    protected SchemaPath innerElem;

    // Can be null - if not, the renderer is accessible.
    // Note that even if the map view "dies", it's renderer will stay around.
    private final @Nullable UIMapView contextView;

    protected EmbedDataTracker embedData = new EmbedDataTracker();

    private Supplier<Boolean> validitySupplier;

    public SchemaHostBase(App app, @Nullable UIMapView rendererSource) {
        super(app);
        contextView = rendererSource;
    }

    protected void replaceValidity() {
        validitySupplier = new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return validitySupplier == this;
            }
        };
    }

    @Override
    public StuffRenderer getContextRenderer() {
        if (contextView != null)
            return contextView.mapTable.renderer;
        return app.stuffRendererIndependent;
    }

    @Override
    public String getContextGUM() {
        if (contextView != null)
            return contextView.mapGUM;
        return null;
    }

    @Override
    public void setEmbedObject(SchemaElement source, IRIO target, String prop, Object dbl) {
        setEmbedObject(innerElem, source, target, prop, dbl);
    }

    @Override
    public Object getEmbedObject(SchemaElement source, IRIO target, String prop, Object def) {
        return getEmbedObject(innerElem, source, target, prop, def);
    }

    @Override
    public Object getEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object def) {
        return embedData.createSlot(locale, source, target, prop, def).value;
    }

    @Override
    public void setEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object dbl) {
        embedData.createSlot(locale, source, target, prop, dbl).value = dbl;
    }

    @Override
    public Supplier<Boolean> getValidity() {
        return validitySupplier;
    }

    @Override
    public ISchemaHost newBlank() {
        return new SchemaHostImpl(app, contextView);
    }

    @Override
    public SchemaPath getCurrentObject() {
        return innerElem;
    }

    @Override
    public void launchOther(UIElement uiTest) {
        app.ui.wm.createWindow(uiTest);
    }

    @Override
    public String toString() {
        if (innerElem == null)
            return "(how'd you manage this then?)";
        String rootName = app.odb.getIdByObject(innerElem.root);
        if (rootName == null)
            rootName = "AnonObject";
        String name = rootName;
        name += innerElem.windowTitleSuffix();
        if (app.odb.getObjectModified(rootName))
            name += "*";
        return name;
    }

    @Override
    public App getApp() {
        return app;
    }
}
