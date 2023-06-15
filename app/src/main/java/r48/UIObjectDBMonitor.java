/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import gabien.ui.theming.Theme;
import gabien.text.TextTools;
import r48.io.IObjectBackend;

/**
 * Used to make sure nothing's leaking memory.
 * Created on 12/29/16.
 */
public class UIObjectDBMonitor extends App.Elm {
    private final TextTools.PlainCached memCache1 = new TextTools.PlainCached();
    private final TextTools.PlainCached memCache2 = new TextTools.PlainCached();

    public UIObjectDBMonitor(App app) {
        super(app);
        setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(320), app.f.scaleGuess(240)));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void renderLayer(IGrDriver igd, UILayer layer) {
        if (layer != UILayer.Base && layer != UILayer.Content)
            return;
        boolean isBackground = layer == UILayer.Base;
        int step = UILabel.getRecommendedTextSize("", app.f.objectDBMonitorTH).height;
        int width = getSize().width;
        Theme theme = getTheme();
        UILabel.drawLabel(theme, igd, width, 0, 0, toString(), Theme.B_TEXTBOX, app.f.objectDBMonitorTH, memCache1, isBackground, !isBackground);
        int oy = step;
        for (String s : UITest.sortedKeysStr(app.odb.objectMap.keySet())) {
            String status = T.u.odb_disposed;
            IObjectBackend.ILoadedObject rio = app.odb.objectMap.get(s).get();
            if (rio != null) {
                status = T.u.odb_listeners.r(app.odb.countModificationListeners(rio));
                if (app.odb.getObjectNewlyCreated(s)) {
                    status += T.u.odb_created;
                } else if (app.odb.getObjectModified(s)) {
                    status += T.u.odb_modified;
                }
            } else {
                if (app.odb.getObjectModified(s)) {
                    status += T.u.odb_lost;
                } else {
                    app.odb.objectMap.remove(s);
                }
            }
            // memCache2 should ideally NOT be shared between these, it's literally the worst thing you can do
            UILabel.drawLabel(theme, igd, width, 0, oy, s + status, Theme.B_LABEL, app.f.objectDBMonitorTH, memCache2, isBackground, !isBackground);
            oy += step;
        }
        setWantedSize(new Size(width, oy));
    }

    @Override
    public String toString() {
        Runtime r = Runtime.getRuntime();
        return ((r.totalMemory() - r.freeMemory()) / (1024 * 1024)) + "/" + (r.totalMemory() / (1024 * 1024)) + "M " + (r.maxMemory() / (1024 * 1024)) + "MX";
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        System.gc();
        return super.handleNewPointer(state);
    }
}
