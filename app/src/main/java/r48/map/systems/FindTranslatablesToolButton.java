/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.systems;

import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.io.IObjectBackend;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.UIMapView;
import r48.maptools.UIMTBase;
import r48.search.CompoundCommandClassifier;
import r48.search.ICommandClassifier;
import r48.search.RMFindTranslatables;
import r48.ui.search.UIClassifierishInstanceWidget;
import r48.ui.search.UICommandSites;

/**
 * Find translatables map logic
 * Extracted from R2kSystem on 30th September 2022
 */
public final class FindTranslatablesToolButton extends ToolButton {
    public final App app;
    public final String ep;
    public FindTranslatablesToolButton(App app, String e) {
        super(app.t.m.bSearchCmds);
        this.app = app;
        ep = e;
    }

    @Override
    public UIMTBase apply(final IMapToolContext a) {
        ICommandClassifier.Instance ccc = CompoundCommandClassifier.I.instance(app);
        UIClassifierishInstanceWidget<ICommandClassifier.Instance> uiccs = new UIClassifierishInstanceWidget<>(app, ccc);
        UISplitterLayout uspl = new UISplitterLayout(uiccs, new UITextButton(app.t.g.bConfirm, app.f.dialogWindowTH, () -> {
            UIMapView umv = a.getMapView();
            final IObjectBackend.ILoadedObject map = umv.map.object;
            UICommandSites ucs = new UICommandSites(umv.app, umv.map.objectId, () -> {
                RMFindTranslatables rft = new RMFindTranslatables(umv.app, map);
                rft.addSitesFromMap(a.getMapView(), ep, ccc);
                return rft.toArray();
            }, new IObjectBackend.ILoadedObject[] {
                map
            });
            ucs.show();
        }), true, 1);
        app.ui.wm.createWindow(uspl, "mSearchCmds");
        return null;
    }
}