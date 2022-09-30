/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.map.systems;

import gabien.uslx.append.ISupplier;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
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
    public FindTranslatablesToolButton() {
        super(TXDB.get("Find Translatables"));
    }

    @Override
    public UIMTBase apply(final IMapToolContext a) {
        UIMapView umv = a.getMapView();
        final IObjectBackend.ILoadedObject map = umv.map.object;
        UICommandSites ucs = new UICommandSites(umv.map.objectId, new ISupplier<CommandSite[]>() {
            @Override
            public CommandSite[] get() {
                RMFindTranslatables rft = new RMFindTranslatables(map);
                rft.addSitesFromMap(a.getMapView());
                return rft.toArray();
            }
        }, new IObjectBackend.ILoadedObject[] {
            map
        });
        ucs.show();
        return null;
    }
}