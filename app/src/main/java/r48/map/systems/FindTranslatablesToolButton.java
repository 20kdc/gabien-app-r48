/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.systems;

import gabien.uslx.append.ISupplier;
import r48.App;
import r48.io.IObjectBackend;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.UIMapView;
import r48.maptools.UIMTBase;
import r48.toolsets.utils.CommandSite;
import r48.toolsets.utils.RMFindTranslatables;
import r48.toolsets.utils.UICommandSites;

/**
 * Find translatables map logic
 * Extracted from R2kSystem on 30th September 2022
 */
public final class FindTranslatablesToolButton extends ToolButton {
    public final String ep;
    public FindTranslatablesToolButton(App app, String e) {
        super(app.t.m.bFindTranslatables);
        ep = e;
    }

    @Override
    public UIMTBase apply(final IMapToolContext a) {
        UIMapView umv = a.getMapView();
        final IObjectBackend.ILoadedObject map = umv.map.object;
        UICommandSites ucs = new UICommandSites(umv.app, umv.map.objectId, new ISupplier<CommandSite[]>() {
            @Override
            public CommandSite[] get() {
                RMFindTranslatables rft = new RMFindTranslatables(umv.app, map);
                rft.addSitesFromMap(a.getMapView(), ep);
                return rft.toArray();
            }
        }, new IObjectBackend.ILoadedObject[] {
            map
        });
        ucs.show();
        return null;
    }
}