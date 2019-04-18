/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.wm;

import gabien.ui.*;
import gabienapp.GrandLauncherUtils;
import r48.AppMain;
import r48.tests.grand.GrandExecutionError;
import r48.ui.UISymbolButton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

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
            if (tx.contents == e)
                clickIcon(tx, ico);
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

    // --- Control-Finding-based access. ---

    public static Rect getControlRect(String id) {
        HashMap<UIElement, Rect> es = getAllControls();
        return es.get(getControlCore(id, es.keySet()));
    }

    public static UIElement getControl(String id) {
        return getControlCore(id, getAllControls().keySet());
    }

    private static UIElement getControlCore(String id, Set<UIElement> es) {
        if (id == null)
            return GrandLauncherUtils.getTicker().runningWindows().getFirst();
        // ¥ splits it up
        String[] st = id.split("¥");
        UIElement currentRoot = null;
        for (String ptr : st) {
            if (ptr.equals("..")) {
                if (currentRoot == null)
                    throw new GrandExecutionError("Cannot find a parent with no context");
                currentRoot = currentRoot.getParent();
                if (currentRoot == null)
                    throw new GrandExecutionError("Cannot find a parent that doesn't exist");
                continue;
            }
            boolean ok = false;
            for (UIElement uie : es) {
                boolean isValidId = false;
                String idx = identify(uie);
                if (ptr.equals(idx)) {
                    isValidId = true;
                } else {
                    idx = identifyCls(uie);
                    if (ptr.equals(idx))
                        isValidId = true;
                }
                if (isValidId) {
                    if (aContainsB(currentRoot, uie)) {
                        currentRoot = uie;
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok)
                throw new GrandExecutionError("Unable to get: '" + id + "' (part: '" + ptr + "')");
        }
        if (currentRoot == null)
            throw new GrandExecutionError("Empty selector");
        return currentRoot;
    }

    private static boolean aContainsB(UIElement a, UIElement b) {
        if (a == null)
            return true;
        while (b != null) {
            if (b == a)
                return true;
            b = b.getParent();
        }
        return false;
    }

    // Should match identify
    private static String identifyCls(UIElement uie) {
        if (uie instanceof UISymbolButton)
            return "symbol";
        if (uie instanceof UITextButton)
            return "button";
        if (uie instanceof UITextBox)
            return "textbox";
        if (uie instanceof UILabel)
            return "label";
        if (uie instanceof UIScrollLayout)
            return "scroll";
        return "?";
    }

    private static String identify(UIElement uie) {
        if (uie instanceof UISymbolButton)
            return "symbol:" + ((UISymbolButton) uie).symbol.name();
        if (uie instanceof UITextButton)
            return "button:" + ((UITextButton) uie).text;
        if (uie instanceof UITextBox)
            return "textbox:" + ((UITextBox) uie).text;
        if (uie instanceof UILabel)
            return "label:" + ((UILabel) uie).text;
        if (uie instanceof UIScrollLayout)
            return "scroll:";
        return "?:" + uie.toString();
    }

    private static String fullIdentify(UIElement uie) {
        UIElement p = uie.getParent();
        if (p != null)
            return fullIdentify(p) + "¥" + identify(uie);
        return identify(uie);
    }

    private static HashMap<UIElement, Rect> getAllControls() {
        HashMap<UIElement, Rect> hs = new HashMap<UIElement, Rect>();
        for (UIElement uie : GrandLauncherUtils.getTicker().runningWindows())
            addElementAndDescendants(uie, hs, new Rect(uie.getSize()));
        return hs;
    }

    private static void addElementAndDescendants(UIElement uie, HashMap<UIElement, Rect> hs, Rect screenRect) {
        hs.put(uie, screenRect);
        // This is the list of things that have descendants
        if (uie instanceof UIElement.UIPanel) {
            for (UIElement s : GrandControlUtils.getDescendantsOf((UIElement.UIPanel) uie))
                addDescendant(uie, hs, screenRect, s);
        } else if (uie instanceof UIWindowView) {
            for (UIWindowView.IShell shl : ((UIWindowView) uie).getShells()) {
                if (shl instanceof UIWindowView.ElementShell) {
                    addDescendant(uie, hs, screenRect, ((UIWindowView.ElementShell) shl).uie);
                } else if (shl instanceof UIWindowView.TabShell) {
                    addDescendant(uie, hs, screenRect, ((UIWindowView.TabShell) shl).contents);
                }
            }
        } else if (uie instanceof UIElement.UIProxy) {
            addElementAndDescendants(GrandControlUtils.getProxyContent((UIElement.UIProxy) uie), hs, screenRect);
        }
    }

    private static void addDescendant(UIElement uie, HashMap<UIElement, Rect> hs, Rect screenRect, UIElement s) {
        Rect intersect = new Rect(uie.getSize()).getIntersection(s.getParentRelativeBounds());
        if (intersect == null)
            return;
        // advance!
        screenRect = new Rect(screenRect.x + intersect.x, screenRect.y + intersect.y, intersect.width, intersect.height);
        addElementAndDescendants(s, hs, screenRect);
    }

    public static void printTree() {
        // The quotes ensure that the resulting paths include 'hidden' spaces
        for (UIElement uie : getAllControls().keySet())
            System.out.println("\"" + fullIdentify(uie) + "\"");
    }
}
