/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;

/**
 * Used to make sure nothing's leaking memory.
 * Created on 12/29/16.
 */
public class UIObjectDBMonitor extends UIElement {
    public UIObjectDBMonitor() {
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
        for (String s : UITest.sortedKeysStr(AppMain.objectDB.objectMap.keySet())) {
            String status = TXDB.get(" [disposed]");
            IObjectBackend.ILoadedObject rio = AppMain.objectDB.objectMap.get(s).get();
            if (rio != null) {
                status = FormatSyntax.formatExtended(TXDB.get(" #[#AML#]"), new RubyIO().setFX(AppMain.objectDB.countModificationListeners(rio)));
                if (AppMain.objectDB.getObjectNewlyCreated(s)) {
                    status += TXDB.get(" [created]");
                } else if (AppMain.objectDB.getObjectModified(s)) {
                    status += TXDB.get(" [modified]");
                }
            } else {
                if (AppMain.objectDB.getObjectModified(s)) {
                    status += TXDB.get(" [modifications lost, should never occur!]");
                } else {
                    AppMain.objectDB.objectMap.remove(s);
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
