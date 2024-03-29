/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.search;

import java.util.function.Consumer;
import java.util.function.Supplier;

import gabien.ui.UIElement;
import gabien.ui.layouts.UIScrollLayout;
import gabien.uslx.append.Rect;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.IObjectBackend;
import r48.schema.util.SchemaPath;
import r48.search.CommandSite;

/**
 * Created on 17th September 2022
 */
public class UICommandSites extends App.Prx {
    private final Supplier<CommandSite[]> refresh;
    private final IObjectBackend.ILoadedObject[] roots;

    private final UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
    private boolean needsRefresh = false;
    private String objIdName;

    private final Consumer<SchemaPath> consumer = new Consumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath t) {
            needsRefresh = true;
        }
    };

    public UICommandSites(App app, String name, Supplier<CommandSite[]> supplier, IObjectBackend.ILoadedObject[] r) {
        super(app);
        objIdName = name;
        refresh = supplier;
        roots = r;
        for (IObjectBackend.ILoadedObject ilo : roots)
            app.odb.registerModificationHandler(ilo, consumer);
        doRefresh();
        proxySetElement(layout, true);
        setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(400), app.f.scaleGuess(300)));
    }

    public void show() {
        app.ui.wm.createWindow(this, "findTranslatables");
    }

    @Override
    public String toString() {
        return T.u.mTranslatablesIn.r(objIdName);
    }

    public void doRefresh() {
        CommandSite[] sites = refresh.get();
        UIElement[] siteElements = new UIElement[sites.length];
        for (int i = 0; i < sites.length; i++)
            siteElements[i] = sites[i].element;
        layout.panelsSet(siteElements);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (needsRefresh) {
            needsRefresh = false;
            doRefresh();
        }
    }

    @Override
    public void onWindowClose() {
        super.onWindowClose();
        for (IObjectBackend.ILoadedObject ilo : roots)
            app.odb.deregisterModificationHandler(ilo, consumer);
    }
}
