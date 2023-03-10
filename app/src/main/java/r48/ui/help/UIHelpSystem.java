/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.*;
import gabien.ui.UIElement.UIPanel;
import gabien.uslx.append.*;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.ui.UIThumbnail;

import java.util.LinkedList;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIPanel implements IConsumer<String> {
    public IConsumer<String> onLinkClick;

    public LinkedList<HelpElement> page = new LinkedList<HelpElement>();

    public final Config c;
    public final InterlaunchGlobals ilg;

    public UIHelpSystem(InterlaunchGlobals ilg) {
        this.c = ilg.c;
        this.ilg = ilg;
        Rect sz = new Rect(0, 0, c.f.helpTH * 32, c.f.helpTH * 32);
        setWantedSize(sz);
        setForcedBounds(null, sz);
    }

    @Override
    public void runLayout() {
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        Size s = getSize();
        int y = 0;
        // Acts as a limiting factor at all times, so reset when setting rightY to -1.
        int rightX = s.width;
        // NOTE: This is set to -1 as a signal that the 'right-side-panel' is no longer being concatenated,
        //  so a new right-side-panel should be setup at the current anchor.
        int rightY = -1;
        for (HelpElement he : page) {
            if (y >= rightY) {
                rightY = -1;
                rightX = s.width;
            }
            he.onLinkClick = this;
            layoutAddElement(he.element);
            Size ws = he.element.getWantedSize();
            boolean effectivePosition = he.position;
            if (ws.width > s.width)
                effectivePosition = false;
            if (!effectivePosition) {
                // Centre/left.
                he.element.setForcedBounds(this, new Rect(0, y, rightX, ws.height));
                y += ws.height;
            } else {
                if (rightY == -1)
                    rightY = y;
                // Right side.
                he.element.setForcedBounds(this, new Rect(s.width - ws.width, rightY, ws.width, ws.height));
                rightX = Math.min(s.width - ws.width, rightX);
                rightY += ws.height;
            }
        }
        setWantedSize(new Size(1, Math.max(y, rightY)));
    }

    @Override
    public void accept(String integer) {
        onLinkClick.accept(integer);
    }

    public static class HelpElement {
        public final UIElement element;
        // Set by the UIHelpSystem.
        private IConsumer<String> onLinkClick;
        public final boolean position;

        public HelpElement(Config c, char ch, String arg) {
            String[] args = arg.split(" ");
            if (ch == '.') {
                position = false;
                StringBuilder sbt = new StringBuilder();
                for (String s : args) {
                    sbt.append(s);
                    sbt.append(' ');
                }
                element = new UILabel(sbt.toString(), c.f.helpTH);
            } else if (ch == 'h') {
                position = false;
                StringBuilder sbt = new StringBuilder();
                for (String s : args) {
                    sbt.append(s);
                    sbt.append(' ');
                }
                element = new UILabel(sbt.toString(), c.f.helpTH).centred();
            } else if (ch == '>') {
                String t = "";
                boolean first = true;
                for (String s : args) {
                    if (first) {
                        first = false;
                    } else {
                        t += s + " ";
                    }
                }
                final String index = args[0];
                position = false;
                element = new UITextButton(t, c.f.helpLinkH, new Runnable() {
                    @Override
                    public void run() {
                        if (onLinkClick != null)
                            onLinkClick.accept(index);
                    }
                });
            } else if (ch == 'p') {
                position = false;
                element = new UIPublicPanel(0, c.f.scaleGuess(Integer.parseInt(args[0])));
            } else if ((ch == 'i') || (ch == 'I')) {
                final IImage r = GaBIEn.getImageEx(args[0], false, true);
                boolean extended = args.length > 1;
                boolean extended2 = args.length > 5;
                // uiGuessScaler takes over
                final int xx = extended ? Integer.parseInt(args[1]) : 0;
                final int yy = extended ? Integer.parseInt(args[2]) : 0;
                final int sw = extended ? Integer.parseInt(args[3]) : r.getWidth();
                final int sh = extended ? Integer.parseInt(args[4]) : r.getHeight();
                final int w = c.f.scaleGuess(extended2 ? Integer.parseInt(args[5]) : sw);
                UIThumbnail uie = new UIThumbnail(r, w, new Rect(xx, yy, sw, sh));
                position = ch == 'i';
                element = uie;
            } else {
                throw new RuntimeException("Cannot handle!");
            }
        }
    }
}
