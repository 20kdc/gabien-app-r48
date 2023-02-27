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
import r48.dbs.TXDB;
import r48.io.IObjectBackend;

/**
 * Used to make sure nothing's leaking memory.
 * Created on 12/29/16.
 */
public class UIObjectDBMonitor extends UIElement {
    public final App app;
    public UIObjectDBMonitor(App app) {
        this.app = app;
        setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(320), FontSizes.scaleGuess(240)));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void render(IGrDriver igd) {
        int step = UILabel.getRecommendedTextSize("", FontSizes.objectDBMonitorTextHeight).height;
        int width = getSize().width;
        UILabel.drawLabel(igd, width, 0, 0, toString(), 1, FontSizes.objectDBMonitorTextHeight);
        int oy = step;
        for (String s : UITest.sortedKeysStr(app.odb.objectMap.keySet())) {
            String status = TXDB.get(" [disposed]");
            IObjectBackend.ILoadedObject rio = app.odb.objectMap.get(s).get();
            if (rio != null) {
                status = app.fmt.formatExtended(TXDB.get(" #[#AML#]"), new RubyIO().setFX(app.odb.countModificationListeners(rio)));
                if (app.odb.getObjectNewlyCreated(s)) {
                    status += TXDB.get(" [created]");
                } else if (app.odb.getObjectModified(s)) {
                    status += TXDB.get(" [modified]");
                }
            } else {
                if (app.odb.getObjectModified(s)) {
                    status += TXDB.get(" [modifications lost, should never occur!]");
                } else {
                    app.odb.objectMap.remove(s);
                }
            }
            UILabel.drawLabel(igd, width, 0, oy, s + status, 0, FontSizes.objectDBMonitorTextHeight);
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
