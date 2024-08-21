/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.ui.*;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map.UIMapView;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted from SchemaHostImpl, 17th July, 2023.
 */
public abstract class SchemaHostBase extends App.Pan implements ISchemaHost {
    protected SchemaPath innerElem;

    protected Consumer<SchemaPath> nudgeRunnable = new Consumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            nudged = true;
        }
    };
    protected boolean nudged = false;

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
    public @NonNull StuffRenderer getContextRenderer() {
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
    public <T> EmbedDataSlot<T> embedSlot(SchemaPath locale, IRIO target, EmbedDataKey<T> prop, T def) {
        return embedData.createSlot(locale, target, prop, def);
    }

    @Override
    public Supplier<Boolean> getValidity() {
        return validitySupplier;
    }

    @Override
    public ISchemaHost newBlank() {
        return new UISchemaHostWindow(app, contextView);
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
        String name = innerElem.root.toString();
        name += innerElem.windowTitleSuffix();
        if (app.odb.modifiedObjects.contains(innerElem.root))
            name += "*";
        return name;
    }

    @Override
    public App getApp() {
        return app;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (nudged) {
            replaceValidity();
            refreshDisplay();
            nudged = false;
        }
    }

    /**
     * Implements the actual 'switch' operation.
     */
    protected final void switchObject(SchemaPath nextObject) {
        // switch over listeners, validity, state
        if (innerElem != null)
            innerElem.root.deregisterModificationHandler(nudgeRunnable);
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        nextObject.root.registerModificationHandler(nudgeRunnable);
        replaceValidity();
        innerElem = nextObject;
        // update
        refreshDisplay();
    }

    /**
     * Implements the actual 'switch' operation for the UI.
     */
    protected abstract void refreshDisplay();
}
