/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.ui.*;
import gabien.wsi.IPeripherals;
import r48.R48;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.ui.AppUI;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted from SchemaHostImpl, 17th July, 2023.
 */
public abstract class SchemaHostBase extends AppUI.Pan implements ISchemaHost {
    protected SchemaPath innerElem;

    protected Consumer<SchemaPath> nudgeRunnable = new Consumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            nudged = true;
        }
    };
    protected boolean nudged = false;

    public final @NonNull SchemaDynamicContext dynContext;

    protected EmbedDataTracker embedData = new EmbedDataTracker();

    private Supplier<Boolean> validitySupplier;

    // Make a copy of this when necessary.
    protected final HashMap<String, DMKey> operatorContext = new HashMap<>();

    public SchemaHostBase(@NonNull AppUI appUI, @Nullable SchemaDynamicContext dynContext) {
        super(appUI);
        this.dynContext = dynContext == null ? new SchemaDynamicContext(app, null, appUI) : dynContext;
    }

    @Override
    public SchemaDynamicContext getContext() {
        return dynContext;
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
    public <T> EmbedDataSlot<T> embedSlot(SchemaPath locale, IRIO target, EmbedDataKey<T> prop, T def) {
        return embedData.createSlot(locale, target, prop, def);
    }

    @Override
    public Supplier<Boolean> getValidity() {
        return validitySupplier;
    }

    @Override
    public ISchemaHost newBlank() {
        return new UISchemaHostWindow(U, dynContext);
    }

    @Override
    public SchemaPath getCurrentObject() {
        return innerElem;
    }

    @Override
    public void launchOther(UIElement uiTest) {
        U.wm.createWindow(uiTest);
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
    public R48 getApp() {
        return app;
    }

    @Override
    public AppUI getAppUI() {
        return U;
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
     * Be sure to clear the operator context!
     */
    protected abstract void refreshDisplay();

    @Override
    public void addOperatorContext(IRIO target, String key, DMKey value) {
        // shouldn't happen but,,,
        if (innerElem == null)
            return;
        // this is to catch very obvious subelements trying to pass off their context upwards
        // we might need some kind of root isolator for this kind of thing... or maybe operator context is overengineered?
        if (this.innerElem.targetElement != target)
            return;
        operatorContext.put(key, value);
    }

    @Override
    public HashMap<String, DMKey> copyOperatorContext() {
        return new HashMap<>(operatorContext);
    }
}
