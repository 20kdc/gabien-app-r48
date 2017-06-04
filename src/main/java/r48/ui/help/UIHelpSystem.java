/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui.help;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIPanel {

    public IConsumer<Integer> onLinkClick;
    public LinkedList<HelpElement> page = new LinkedList<HelpElement>();

    public UIHelpSystem() {
        super.setBounds(new Rect(0, 0, 640, 320));
    }

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
            working.Text = t;
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
        // I'm running analysis tools just for the sake of it. It's complaining about this returning null.
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
            if ((hc.c == '.') || (hc.c == '>')) {
                int vlen = rect.width - imgSize;
                if (vlen < (imgSize / 2)) {
                    y = imgEndY;
                    imgSize = 0;
                }
                vlen = rect.width - imgSize;
                for (UIElement uil : handleThing(hc.c, hc.args, vlen)) {
                    int eh = uil.getBounds().height;
                    uil.setBounds(new Rect(0, y, vlen, eh));
                    allElements.add(uil);
                    y += eh;
                    if (y >= imgEndY)
                        imgSize = 0;
                }
            }
            if ((hc.c == 'i') || (hc.c == 'I')) {
                boolean left = hc.c == 'I';
                final IGrInDriver.IImage r = GaBIEn.getImage(hc.args[0]);
                boolean extended = hc.args.length > 1;
                final int xx = extended ? Integer.parseInt(hc.args[1]) : 0;
                final int yy = extended ? Integer.parseInt(hc.args[2]) : 0;
                final int w = extended ? Integer.parseInt(hc.args[3]) : r.getWidth();
                final int h = extended ? Integer.parseInt(hc.args[4]) : r.getHeight();
                UIPanel uie = new UIPanel();
                uie.baseImage = r;
                uie.imageX = xx;
                uie.imageY = yy;
                if (left) {
                    uie.setBounds(new Rect((rect.width / 2) - (w / 2), y, w, h));
                    y += h;
                } else {
                    uie.setBounds(new Rect(rect.width - w, y, w, h));
                    imgSize = w;
                    imgEndY = y + h;
                }
                allElements.add(uie);
            }
            if (hc.c == 'p')
                y += Integer.parseInt(hc.args[0]);
        }

        super.setBounds(new Rect(rect.x, rect.y, rect.width, Math.max(imgEndY, y)));
    }

    public static class HelpElement {
        char c;
        String[] args;

        public HelpElement(char c, String[] args) {
            this.c = c;
            this.args = args;
        }
    }
}
