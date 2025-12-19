/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.App;

/**
 * SchemaDynamicContext associates various stuff with the 'origin window'.
 * At some point, it honestly might be phased out in favour of tracking the active window instead or something.
 * The need for a 'current map' schema that applies to common events kind of burned a hole in the original plan. 
 * Created 19th December, 2025.
 */
public final class SchemaDynamicContext extends App.Svc {
    public UIMapView mapView;

    public SchemaDynamicContext(App app, @Nullable UIMapView mapView) {
        super(app);
        this.mapView = mapView;
    }

    public @NonNull StuffRenderer getRenderer() {
        if (mapView != null)
            return mapView.mapTable.renderer;
        return app.stuffRendererIndependent;
    }

    public String getGUM() {
        if (mapView != null)
            return mapView.mapGUM;
        return null;
    }
}
