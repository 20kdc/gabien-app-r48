/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.wm;

import gabien.ui.UIElement;
import gabien.ui.UITabBar;
import gabien.ui.UIWindowView;
import r48.AppMain;
import r48.tests.grand.GrandExecutionError;

import java.util.LinkedList;

/**
 * Created on March 28, 2019.
 */
public class GrandWindowManagerUtils {
    public static UIElement[] getAllWindows() {
        LinkedList<UIElement> ll = new LinkedList<UIElement>();
        if (AppMain.window == null)
            throw new GrandExecutionError("No window manager");
        for (UITabBar.Tab uww : AppMain.window.tabPane.getTabs())
            ll.add(uww.contents);
        for (UIWindowView uww : AppMain.window.allWindowViews)
            for (UIWindowView.IShell sh : uww.getShells())
                if (sh instanceof UIWindowView.TabShell)
                    ll.add(((UIWindowView.TabShell) sh).contents);
        return ll.toArray(new UIElement[0]);
    }

    public static void clickIcon(UIElement e, int ico) {
        if (AppMain.window == null)
            throw new GrandExecutionError("No window manager");
        for (UITabBar.Tab tx : AppMain.window.tabPane.getTabs()) {
            if (tx.contents == e) {
                clickIcon(tx, ico);
            }
        }
        for (UIWindowView uww : AppMain.window.allWindowViews) {
            for (UIWindowView.IShell sh : uww.getShells()) {
                if (sh instanceof UIWindowView.TabShell) {
                    if (((UIWindowView.TabShell) sh).contents == e) {
                        clickIcon((UIWindowView.TabShell) sh, ico);
                        return;
                    }
                }
            }
        }
    }

    private static void clickIcon(UITabBar.Tab sh, int ico) {
        sh.icons[ico].click(sh);
    }

    public static void selectTab(UIElement element) {
        AppMain.window.tabPane.selectTab(element);
    }
}
