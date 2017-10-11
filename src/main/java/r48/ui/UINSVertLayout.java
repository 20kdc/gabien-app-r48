/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;

/**
 * Used for cases where there's an upper "bar" section, and a lower "main" section.
 * Created on 1/6/17.
 */
public class UINSVertLayout extends UIPanel {
    public UIElement upper;
    public UIElement lower;

    public UINSVertLayout(UIElement a, UIElement b) {
        Rect ab = a.getBounds();
        Rect bb = b.getBounds();
        int w = Math.max(ab.width, bb.width);
        int h = ab.height + bb.height;
        upper = a;
        lower = b;
        setBounds(new Rect(0, 0, w, h));
        allElements.add(upper);
        allElements.add(lower);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        // Run this a while to let upper stabilize. UIHelpSystem should be considered "notorious" for this kind of problem-causing.
        for (int i = 0; i < 4; i++) {
            int uh = upper.getBounds().height;
            upper.setBounds(new Rect(0, 0, r.width, uh));
            lower.setBounds(new Rect(0, uh, r.width, r.height - uh));
        }
    }
}
