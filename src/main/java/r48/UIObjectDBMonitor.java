/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;

/**
 * Used to make sure nothing's leaking memory.
 * Very performance-eating because it does a GC every frame to keep itself accurate.
 * Created on 12/29/16.
 */
public class UIObjectDBMonitor extends UIElement {
    public UIObjectDBMonitor() {
        setBounds(new Rect(0, 0, 320, 240));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        int step = UILabel.getRecommendedSize("", FontSizes.objectDBMonitorTextHeight).height;
        for (String s : UITest.sortedKeysStr(AppMain.objectDB.objectMap.keySet())) {
            String status = TXDB.get(" [disposed]");
            RubyIO rio = AppMain.objectDB.objectMap.get(s).get();
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
            UILabel.drawLabel(igd, getBounds().width, ox, oy, s + status, 0, FontSizes.objectDBMonitorTextHeight);
            oy += step;
        }
    }

    @Override
    public String toString() {
        Runtime r = Runtime.getRuntime();
        return ((r.totalMemory() - r.freeMemory()) / (1024 * 1024)) + "/" + (r.totalMemory() / (1024 * 1024)) + "M " + (r.maxMemory() / (1024 * 1024)) + "MX";
    }

    @Override
    public void handleClick(int x, int y, int button) {
        System.gc();
    }
}
