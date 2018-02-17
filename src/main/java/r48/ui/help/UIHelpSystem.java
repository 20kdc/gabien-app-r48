/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.*;
import r48.FontSizes;

import java.util.LinkedList;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIElement.UIPanel {
    /*IPCRESS*/
    public IConsumer<Integer> onLinkClick;
    public LinkedList<HelpElement> page = new LinkedList<HelpElement>();

    public UIHelpSystem() {
        //super.setBounds(new Rect(0, 0, 640, 320));
    }

    @Override
    public void runLayout() {

    }

    /*IPCRESS
    private UIElement[] handleThing(char c, String[] args, int availableWidth) {
        if (c == '.') {
            String t = "";
            LinkedList<UIElement> results = new LinkedList<UIElement>();
            UILabel working = new UILabel("", FontSizes.helpParagraphStartHeight);
            for (String s : args) {
                String rt = t + s + " ";
                if (UILabel.getRecommendedSize(rt, working.textHeight).width > availableWidth) {
                    working.Text = t;
                    results.add(working);
                    working = new UILabel("", FontSizes.helpTextHeight);
                    rt = s + " ";
                }
                t = rt;
            }
            working.text = t;
            results.add(working);
            return results.toArray(new UIElement[0]);
        }
        if (c == '>') {
            String t = "";
            boolean first = true;
            for (String s : args) {
                if (first) {
                    first = false;
                } else {
                    t += s + " ";
                }
            }
            final int index = Integer.parseInt(args[0]);
            return new UIElement[] {new UITextButton(FontSizes.helpLinkHeight, t, new Runnable() {
                @Override
                public void run() {
                    onLinkClick.accept(index);
                }
            })};
        }
        // I'm running analysis tools just for the sake of it. It's complaining about this returning null. (This made sense at the time.)
        // Then again, it also complained that UIMapView's update & render was too complicated.
        // And complained about a useless OR which I did for code understandability reasons (this flag is always set to true, never false)
        // What good is optimization if I'm not allowed to write easier to understand code and have the optimizer get rid of the odd bits!
        // Then again, it also complained about FloatSchemaElement, about R48ObjectBackend
        // (oh sure so you can totally avoid if empty bodies and also not use a switch because they can't have local variables)...
        // Finally, it complains about .toArray(new Whatever[0]);, which is necessitated by Java generics.
        // (C# is worse due to portability concerns + Mono sometimes acts screwy + DO I NEED TO SAY MORE, but that doesn't mean I like Java.)
        // ...you know, oddly enough, I'm distrusting the code inspector. Perf. concerns or otherwise, I don't care.
        // Manual array copy you can annoy me for though, since the IDE handles this.
        // Here's a help-line just for me in case I have to read said code later and forget.
        // System.arraycopy(src, base, dst, base, len);
        throw new RuntimeException("That character " + c + " shouldn't even GO here.");
    }

    @Override
    public void setBounds(Rect rect) {
        // Commence layout
        int y = 0;
        int imgSize = 0;
        int imgEndY = 0;

        allElements.clear();

        for (HelpElement hc : page) {
            if (y >= imgEndY)
                imgSize = 0;
            if ((hc.c == '.') || (hc.c == '>')) {
                int vlen = rect.width - imgSize;
                for (UIElement uil : handleThing(hc.c, hc.args, vlen)) {
                    int eh = uil.getBounds().height;
                    uil.setBounds(new Rect(0, y, vlen, eh));
                    allElements.add(uil);
                    y += eh;
                }
            }
            if ((hc.c == 'i') || (hc.c == 'I')) {
                boolean left = hc.c == 'I';
                final IImage r = GaBIEn.getImage(hc.args[0]);
                boolean extended = hc.args.length > 1;
                // uiGuessScaler takes over
                final int xx = extended ? Integer.parseInt(hc.args[1]) : 0;
                final int yy = extended ? Integer.parseInt(hc.args[2]) : 0;
                final int sw = extended ? Integer.parseInt(hc.args[3]) : r.getWidth();
                final int sh = extended ? Integer.parseInt(hc.args[4]) : r.getHeight();
                final int w = FontSizes.scaleGuess(sw);
                final int h = FontSizes.scaleGuess(sh);
                UIPublicPanel uie = new UIPublicPanel();
                uie.baseImage = r;
                uie.imageX = xx;
                uie.imageY = yy;
                uie.imageScale = true;
                uie.imageSW = sw;
                uie.imageSH = sh;
                if (!left) {
                    // If there isn't much space, do this anyway (responsive design people looking at this in future would be either approving or hating, if anyone was reading this)
                    if (rect.width < (w * 2))
                        left = true;
                }
                if (left) {
                    if (imgSize != 0) {
                        y = imgEndY;
                        imgSize = 0;
                    }
                    uie.setBounds(new Rect((rect.width / 2) - (w / 2), y, w, h));
                    y += h;
                } else {
                    if (imgSize != 0) {
                        uie.setBounds(new Rect(rect.width - w, imgEndY, w, h));
                        imgSize = Math.max(imgSize, w);
                        imgEndY += h;
                    } else {
                        uie.setBounds(new Rect(rect.width - w, y, w, h));
                        imgSize = w;
                        imgEndY = y + h;
                    }
                }
                allElements.add(uie);
            }
            if (hc.c == 'p')
                y += FontSizes.scaleGuess(Integer.parseInt(hc.args[0]));
        }
        super.setBounds(new Rect(rect.x, rect.y, rect.width, Math.max(imgEndY, y)));
    }*/

    public static class HelpElement {
        char c;
        String[] args;

        public HelpElement(char c, String[] args) {
            this.c = c;
            this.args = args;
        }
    }
}
