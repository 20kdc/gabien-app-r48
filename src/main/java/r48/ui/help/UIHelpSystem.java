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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIElement.UIPanel {
    public IConsumer<Integer> onLinkClick;
    public LinkedList<HelpElement> page = new LinkedList<HelpElement>();
    private int lastWidth = 0;
    private HelpElement lastFirst;

    public UIHelpSystem() {
        page.clear();
        for (int i = 0; i < 8; i++) {
            // You don't want to try translating these and you shouldn't.
            // They're meant to get a rough estimate on a good help window size.
            page.add(new HelpElement('.', "T'was brillig in the slithy toves, did Gireth gimble in the wabe.".split(" ")));
            page.add(new HelpElement('.', "All mimsy were the borogroves".split(" ")));
        }
        runLayout();
        setForcedBounds(null, new Rect(getWantedSize()));
        page.clear();
    }

    // The thing about the UI rewrite for this,
    //  is that gabien now has a system that splits labels.
    // The trouble is, it doesn't account for the *image* factor.
    private UIElement[] handleThing(char c, String[] args, int availableWidth, AtomicInteger writeOriginalEstimate, int originalEstimateBoost) {
        if (c == '.') {
            String t = "";
            LinkedList<UIElement> results = new LinkedList<UIElement>();
            int textHeight = FontSizes.helpParagraphStartHeight;
            // for original estimate
            StringBuilder sbt = new StringBuilder();
            for (String s : args) {
                sbt.append(s);
                sbt.append(' ');
            }
            UILabel working = new UILabel(sbt.toString(), FontSizes.helpParagraphStartHeight);
            writeOriginalEstimate.set(Math.max(writeOriginalEstimate.get(), working.getWantedSize().width + originalEstimateBoost));
            // original estimate done, get back to work
            working.text = "";
            for (String s : args) {
                String rt = t + s + " ";
                if (UILabel.getRecommendedTextSize(rt, textHeight).width > availableWidth) {
                    working.text = t;
                    working.runLayout();
                    results.add(working);
                    textHeight = FontSizes.helpTextHeight;
                    working = new UILabel("", FontSizes.helpTextHeight);
                    rt = s + " ";
                }
                t = rt;
            }
            working.text = t;
            working.runLayout();
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
            UITextButton button = new UITextButton(t, FontSizes.helpLinkHeight, new Runnable() {
                @Override
                public void run() {
                    onLinkClick.accept(index);
                }
            });
            writeOriginalEstimate.set(Math.max(writeOriginalEstimate.get(), button.getWantedSize().width + originalEstimateBoost));
            return new UIElement[] {button};
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
    public void runLayout() {
        Size rect = getSize();
        if (lastWidth == rect.width)
            if (page.size() != 0)
                if ((page.getFirst() == lastFirst))
                    return;
        lastWidth = rect.width;
        if (page.size() != 0)
            lastFirst = page.getFirst();
        // Commence layout
        int y = 0;
        int imgSize = 0;
        int imgEndY = 0;

        AtomicInteger recommendedW = new AtomicInteger(0);

        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);

        for (HelpElement hc : page) {
            if (y >= imgEndY)
                imgSize = 0;
            if ((hc.c == '.') || (hc.c == '>')) {
                int vlen = rect.width - imgSize;
                for (UIElement uil : handleThing(hc.c, hc.args, vlen, recommendedW, imgSize)) {
                    int eh = uil.getWantedSize().height;
                    uil.setForcedBounds(null, new Rect(0, y, vlen, eh));
                    eh = uil.getWantedSize().height;
                    uil.setForcedBounds(null, new Rect(0, y, vlen, eh));
                    layoutAddElement(uil);
                    y += eh;
                }
            }
            if ((hc.c == 'i') || (hc.c == 'I')) {
                boolean left = hc.c == 'I';
                final IImage r = GaBIEn.getImageEx(hc.args[0], false, true);
                boolean extended = hc.args.length > 1;
                // uiGuessScaler takes over
                final int xx = extended ? Integer.parseInt(hc.args[1]) : 0;
                final int yy = extended ? Integer.parseInt(hc.args[2]) : 0;
                final int sw = extended ? Integer.parseInt(hc.args[3]) : r.getWidth();
                final int sh = extended ? Integer.parseInt(hc.args[4]) : r.getHeight();
                final int w = FontSizes.scaleGuess(sw);
                final int h = FontSizes.scaleGuess(sh);
                if (!left) {
                    // If there isn't much space, do this anyway (responsive design people looking at this in future would be either approving or hating, if anyone was reading this)
                    if (rect.width < (w * 2))
                        left = true;
                }
                Rect bounds;
                if (left) {
                    if (imgSize != 0) {
                        y = imgEndY;
                        imgSize = 0;
                    }
                    bounds = new Rect((rect.width / 2) - (w / 2), y, w, h);
                    y += h;
                } else {
                    if (imgSize != 0) {
                        bounds = new Rect(rect.width - w, imgEndY, w, h);
                        imgSize = Math.max(imgSize, w);
                        imgEndY += h;
                    } else {
                        bounds = new Rect(rect.width - w, y, w, h);
                        imgSize = w;
                        imgEndY = y + h;
                    }
                }
                UIPublicPanel uie = new UIPublicPanel(bounds.width, bounds.height);
                uie.baseImage = r;
                uie.imageX = xx;
                uie.imageY = yy;
                uie.imageScale = true;
                uie.imageSW = sw;
                uie.imageSH = sh;
                uie.setForcedBounds(null, bounds);
                layoutAddElement(uie);
                recommendedW.set(Math.max(recommendedW.get(), imgSize));
            }
            if (hc.c == 'p')
                y += FontSizes.scaleGuess(Integer.parseInt(hc.args[0]));
        }
        setWantedSize(new Size(recommendedW.get(), Math.max(imgEndY, y)));
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
