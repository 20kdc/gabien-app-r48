/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.pva.PVAFrameDrawable;
import gabien.render.IDrawable;
import gabien.ui.*;
import gabien.ui.UIElement.UIPanel;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.elements.UIThumbnail;
import gabien.uslx.append.*;
import r48.app.Coco;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;

import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIPanel implements Consumer<String> {
    public Consumer<String> onLinkClick;

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
    protected void layoutRunImpl() {
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        Size s = getSize();
        int leftY = 0;
        // Acts as a limiting factor at all times, so reset when setting rightY to -1.
        int rightX = s.width;
        // NOTE: This is set to -1 as a signal that the 'right-side-panel' is no longer being concatenated,
        //  so a new right-side-panel should be setup at the current anchor.
        int rightY = -1;
        for (HelpElement he : page) {
            if (leftY >= rightY) {
                rightY = -1;
                rightX = s.width;
            }
            he.onLinkClick = this;
            layoutAddElement(he.element);
            Size wantedSize = he.element.getWantedSize();
            boolean effectivePosition = he.position;
            if (wantedSize.width > s.width)
                effectivePosition = false;
            if (!effectivePosition) {
                // Centre/left.
                int wantedHeight = he.element.layoutGetHForW(rightX);
                he.element.setForcedBounds(this, new Rect(0, leftY, rightX, wantedHeight));
                leftY += wantedHeight;
            } else {
                if (rightY == -1)
                    rightY = leftY;
                // Right side.
                int gottenWidth = Math.min(s.width, wantedSize.width);
                int gottenX = s.width - gottenWidth;
                int wantedHeight = he.element.layoutGetHForW(gottenWidth);
                he.element.setForcedBounds(this, new Rect(gottenX, rightY, gottenWidth, wantedHeight));
                rightX = Math.min(gottenX, rightX);
                rightY += wantedHeight;
            }
        }
    }

    @Override
    public int layoutGetHForW(int width) {
        int leftY = 0;
        // Acts as a limiting factor at all times, so reset when setting rightY to -1.
        int rightX = width;
        // NOTE: This is set to -1 as a signal that the 'right-side-panel' is no longer being concatenated,
        //  so a new right-side-panel should be setup at the current anchor.
        int rightY = -1;
        for (HelpElement he : page) {
            if (leftY >= rightY) {
                rightY = -1;
                rightX = width;
            }
            Size wantedSize = he.element.getWantedSize();
            boolean effectivePosition = he.position;
            if (wantedSize.width > width)
                effectivePosition = false;
            if (!effectivePosition) {
                // Centre/left.
                leftY += he.element.layoutGetHForW(rightX);
            } else {
                if (rightY == -1)
                    rightY = leftY;
                // Right side.
                int gottenWidth = Math.min(width, wantedSize.width);
                int gottenX = width - gottenWidth;
                int wantedHeight = he.element.layoutGetHForW(gottenWidth);
                rightX = Math.min(gottenX, rightX);
                rightY += wantedHeight;
            }
        }
        return Math.max(leftY, rightY);
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        int leftY = 0;
        int leftWidth = 0;
        int rightWidth = 0;
        int maxWidth = 0;
        // NOTE: This is set to -1 as a signal that the 'right-side-panel' is no longer being concatenated,
        //  so a new right-side-panel should be setup at the current anchor.
        int rightY = -1;
        for (HelpElement he : page) {
            if (leftY >= rightY) {
                rightY = -1;
                rightWidth = 0;
            }
            Size ws = he.element.getWantedSize();
            if (!he.position) {
                // Centre/left.
                leftY += ws.height;
                leftWidth = ws.width;
            } else {
                if (rightY == -1)
                    rightY = leftY;
                // Right side.
                rightY += ws.height;
                rightWidth = ws.width;
            }
            int totalWidth = leftWidth + rightWidth;
            if (totalWidth > maxWidth)
                maxWidth = totalWidth;
        }
        return new Size(maxWidth, Math.max(leftY, rightY));
    }

    @Override
    public void accept(String integer) {
        onLinkClick.accept(integer);
    }

    public static class HelpElement {
        public final UIElement element;
        // Set by the UIHelpSystem.
        private Consumer<String> onLinkClick;
        public final boolean position;

        public HelpElement(InterlaunchGlobals ilg, String ch, String arg) {
            Config c = ilg.c;
            String[] args = arg.split(" ");
            if (ch.equals(".")) {
                position = false;
                StringBuilder sbt = new StringBuilder();
                for (String s : args) {
                    sbt.append(s);
                    sbt.append(' ');
                }
                element = new UILabel(sbt.toString(), c.f.helpTH);
            } else if (ch.equals("h")) {
                position = false;
                StringBuilder sbt = new StringBuilder();
                for (String s : args) {
                    sbt.append(s);
                    sbt.append(' ');
                }
                String string = sbt.toString();
                if (string.equals("{PROGRAM_VERSION} "))
                    string = Coco.getVersion();
                element = new UILabel(string, c.f.helpTH).centred();
            } else if (ch.equals(">")) {
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
            } else if (ch.equals("p")) {
                position = false;
                element = new UIEmpty(0, c.f.scaleGuess(Integer.parseInt(args[0])));
            } else if (ch.equals("i") || ch.equals("I")) {
                IDrawable r;
                if (args[0].equals("R48LOGO")) {
                    r = new PVAFrameDrawable(ilg.a.r48Logo, ilg.a.r48Logo.pvaFile.frames.length - 1);
                    float square = r.getRegionHeight();
                    r = r.subRegion((r.getRegionWidth() - square) / 2, 0, square, square);
                } else {
                    r = GaBIEn.getImageEx(args[0], false, true);
                }
                boolean extended = args.length > 1;
                boolean extended2 = args.length > 5;
                // uiGuessScaler takes over
                final int xx = extended ? Integer.parseInt(args[1]) : 0;
                final int yy = extended ? Integer.parseInt(args[2]) : 0;
                final float sw = extended ? Integer.parseInt(args[3]) : r.getRegionWidth();
                final float sh = extended ? Integer.parseInt(args[4]) : r.getRegionHeight();
                final int w = c.f.scaleGuess(extended2 ? Integer.parseInt(args[5]) : (int) sw);
                r = r.subRegion(xx, yy, sw, sh);
                UIThumbnail uie = new UIThumbnail(r, w);
                position = ch.equals("i");
                element = uie;
            } else {
                throw new RuntimeException("Cannot handle!");
            }
        }
    }

    public void tightlyCoupledLayoutRecalculateMetrics() {
        layoutRecalculateMetrics();
    }
}
