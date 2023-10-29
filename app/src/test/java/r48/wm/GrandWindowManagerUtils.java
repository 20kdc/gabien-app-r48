/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.wm;

import gabien.TestKickstart;
import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UIWindowView;
import gabien.uslx.append.Rect;
import r48.App;
import r48.tests.grand.GrandExecutionError;
import r48.tests.grand.GrandTestBuilder;
import r48.ui.UISymbolButton;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created on March 28, 2019.
 */
public class GrandWindowManagerUtils {
    public final TestKickstart kick;
    public final GrandTestBuilder gtb;
    public GrandWindowManagerUtils(GrandTestBuilder k) {
        gtb = k;
        kick = k.kick;
    }

    public App getApp() {
        return gtb.lUtils.getApp();
    }

    public WindowCreatingUIElementConsumer getTicker() {
        return gtb.lUtils.getTicker();
    }

    public UIElement[] getAllWindows() {
        App app = getApp();
        LinkedList<UIElement> ll = new LinkedList<UIElement>();
        if (app.ui.wm == null)
            throw new GrandExecutionError("No window manager");
        for (UITabBar.Tab uww : app.ui.wm.tabPane.getTabs())
            ll.add(uww.contents);
        for (UIWindowView uww : app.ui.wm.allWindowViews)
            for (UIWindowView.IShell sh : uww.getShells())
                if (sh instanceof UIWindowView.TabShell)
                    ll.add(((UIWindowView.TabShell) sh).contents);
        return ll.toArray(new UIElement[0]);
    }

    public void clickIcon(UIElement e, int ico) {
        App app = getApp();
        if (app.ui.wm == null)
            throw new GrandExecutionError("No window manager");
        for (UITabBar.Tab tx : app.ui.wm.tabPane.getTabs()) {
            if (tx.contents == e)
                clickIcon(tx, ico);
        }
        for (UIWindowView uww : app.ui.wm.allWindowViews) {
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

    private void clickIcon(UITabBar.Tab sh, int ico) {
        sh.icons[ico].click(sh);
    }

    public void selectTab(UIElement element) {
        App app = getApp();
        app.ui.wm.tabPane.selectTab(element);
    }

    // --- Control-Finding-based access. ---

    public Rect getControlRect(String id) {
        HashMap<UIElement, Rect> es = getAllControls();
        return es.get(getControlCore(id, es.keySet()));
    }

    public UIElement getControl(String id) {
        return getControlCore(id, getAllControls().keySet());
    }

    private UIElement getControlCore(String id, Set<UIElement> es) {
        if (id == null)
            return getTicker().runningWindows().getFirst();
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
            if (!ok) {
                try {
                    FileOutputStream debugOut = new FileOutputStream("test-out/elements.txt");
                    for (UIElement uie : es) {
                        String res = "[" + identify(uie) + "] [" + identifyCls(uie) + "]\n";
                        debugOut.write(res.getBytes(StandardCharsets.UTF_8));
                    }
                    debugOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                throw new GrandExecutionError("Unable to get: '" + id + "' (part: '" + ptr + "') in phase " + kick.currentTestPhase);
            }
        }
        if (currentRoot == null)
            throw new GrandExecutionError("Empty selector in phase " + kick.currentTestPhase);
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
            return "button:" + ((UITextButton) uie).getText();
        if (uie instanceof UITextBox)
            return "textbox:" + ((UITextBox) uie).getText();
        if (uie instanceof UILabel)
            return "label:" + ((UILabel) uie).getText();
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

    private HashMap<UIElement, Rect> getAllControls() {
        HashMap<UIElement, Rect> hs = new HashMap<UIElement, Rect>();
        for (UIElement uie : getTicker().runningWindows())
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
        // advance!
        Rect sz = s.getParentRelativeBounds();
        screenRect = new Rect(screenRect.x + sz.x, screenRect.y + sz.y, sz.width, sz.height);
        addElementAndDescendants(s, hs, screenRect);
    }

    public void printTree() {
        // The quotes ensure that the resulting paths include 'hidden' spaces
        for (UIElement uie : getAllControls().keySet())
            System.out.println("\"" + fullIdentify(uie) + "\"");
    }
}
