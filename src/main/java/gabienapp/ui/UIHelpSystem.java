/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabienapp.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.dbs.DBLoader;
import gabienapp.dbs.IDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helping things along where needed.
 * Created on 1/25/17.
 */
public class UIHelpSystem extends UIPanel {

    private UILabel pageName;
    public Runnable onLoad;

    public UIHelpSystem(UILabel uil, Runnable ol) {
        super.setBounds(new Rect(0, 0, 640, 480));
        onLoad = ol;
        pageName = uil;
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(new Rect(r.x, r.y, 640, 480));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        super.updateAndRender(ox, oy, deltaTime, selected, igd);
    }

    public void loadPage(final int i) {
        try {
            allElements.clear();
            new DBLoader(new BufferedReader(new InputStreamReader(GaBIEn.getResource("Help.txt"))), new IDatabase() {
                boolean working = false;
                int y = 0;
                int imgSize = 0;
                int imgEndY = 0;
                @Override
                public void newObj(int objId, String objName) throws IOException {
                    if (objId == i) {
                        pageName.Text = objName;
                        working = true;
                    } else {
                        working = false;
                    }
                }

                private UIElement handleThing(char c, String[] args) {
                    if (c == '.') {
                        String t = "";
                        for (String s : args)
                            t += s + " ";
                        return new UILabel(t, false);
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
                        return new UITextButton(false, t, new Runnable() {
                            @Override
                            public void run() {
                                loadPage(index);
                            }
                        });
                    }
                    return null;
                }

                @Override
                public void execCmd(char c, String[] args) throws IOException {
                    if (working) {
                        if ((c == '.') || (c == '>')) {
                            int vlen = 640 - imgSize;
                            UIElement uil = handleThing(c, args);
                            int eh = uil.getBounds().height;
                            uil.setBounds(new Rect(0, y, vlen, eh));
                            allElements.add(uil);
                            y += eh;
                            if (y >= imgEndY)
                                imgSize = 0;
                        }
                        if (c == 'i') {
                            final IGrInDriver.IImage r = GaBIEn.getImage(args[0], 0, 0, 0);
                            UIElement uie = new UIElement() {
                                @Override
                                public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
                                    igd.blitImage(0, 0, r.getWidth(), r.getHeight(), ox, oy, r);
                                }

                                @Override
                                public void handleClick(int x, int y, int button) {
                                }
                            };
                            uie.setBounds(new Rect(640 - r.getWidth(), y, r.getWidth(), r.getHeight()));
                            imgSize = r.getWidth();
                            imgEndY = y + r.getHeight();
                            allElements.add(uie);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        onLoad.run();
    }
}
