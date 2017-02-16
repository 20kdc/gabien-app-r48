/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIPanel {

    private UILabel pageName;
    public Runnable onLoad;
    private String helpFile;

    private LinkedList<HelpElement> page = new LinkedList<HelpElement>();

    public UIHelpSystem(UILabel uil, Runnable ol, String file) {
        super.setBounds(new Rect(0, 0, 640, 320));
        onLoad = ol;
        pageName = uil;
        helpFile = file;
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
                    loadPage(index);
                }
            })};
        }
        return null;
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
                final IGrInDriver.IImage r = GaBIEn.getImage(hc.args[0], 0, 0, 0);
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

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        super.updateAndRender(ox, oy, deltaTime, selected, igd);
    }

    public void loadPage(final int i) {
        try {
            page.clear();
            new DBLoader(new BufferedReader(new InputStreamReader(getHelpStream())), new IDatabase() {
                boolean working = false;
                @Override
                public void newObj(int objId, String objName) throws IOException {
                    if (objId == i) {
                        if (pageName != null)
                            pageName.Text = objName;
                        working = true;
                    } else {
                        working = false;
                    }
                }

                @Override
                public void execCmd(char c, String[] args) throws IOException {
                    if (working)
                        page.add(new HelpElement(c, args));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        setBounds(getBounds());
        if (onLoad != null)
            onLoad.run();
    }

    private InputStream getHelpStream() {
        if (helpFile == null)
            return GaBIEn.getResource("Help.txt");
        return GaBIEn.getFile(helpFile);
    }

    private class HelpElement {
        char c;
        String[] args;

        public HelpElement(char c, String[] args) {
            this.c = c;
            this.args = args;
        }
    }
}
