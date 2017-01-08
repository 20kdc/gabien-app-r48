/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.IGrInDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabienapp.dbs.ObjectDB;

/**
 * Used to make sure nothing's leaking memory.
 * Very performance-eating because it does a GC every frame to keep itself accurate.
 * Created on 12/29/16.
 */
public class UIObjectDBMonitor extends UIElement {
    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        System.gc();
        for (String s : UITest.sortedKeysStr(Application.objectDB.objectMap.keySet())) {
            String status = " [disposed]";
            RubyIO rio = Application.objectDB.objectMap.get(s).get();
            if (rio != null) {
                status = " [" + Application.objectDB.countModificationListeners(rio) + "ML]";
                if (Application.objectDB.getObjectModified(s))
                    status += " [Modified]";
            } else {
                if (Application.objectDB.getObjectModified(s)) {
                    status += " [Modifications lost]";
                } else {
                    Application.objectDB.objectMap.remove(s);
                }
            }
            UILabel.drawLabel(igd, getBounds().width, ox, oy, s + status, false);
            oy += 9;
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {

    }
}
