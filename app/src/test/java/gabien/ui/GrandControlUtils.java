/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien.ui;

import java.util.LinkedList;

/**
 * Created on April 18, 2019.
 */
public class GrandControlUtils {
    public static LinkedList<UIElement> getDescendantsOf(UIElement.UIPanel uie) {
        return uie.layoutGetElements();
    }

    public static UIElement getProxyContent(UIElement.UIProxy uie) {
        return uie.proxyGetElement();
    }

    public static void scroll(UIScrollLayout ctrl, UIElement control) {
        int sh = ctrl.calcScrollHeight(ctrl.getSize());
        if (sh != 0) {
            ctrl.scrollbar.scrollPoint += control.getParentRelativeBounds().y / (double) sh;
            if (ctrl.scrollbar.scrollPoint < 0)
                ctrl.scrollbar.scrollPoint = 0;
            if (ctrl.scrollbar.scrollPoint >= 1)
                ctrl.scrollbar.scrollPoint = 1;
        }
    }
}
