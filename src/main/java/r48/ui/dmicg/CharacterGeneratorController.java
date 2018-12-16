/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dmicg;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;
import r48.ui.UIColourSwatchButton;
import r48.ui.dialog.UIColourPicker;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Character Generator
 * Demetrius wants this, so I'm doing this.
 * Created on December 16, 2018.
 */
public class CharacterGeneratorController {
    public UIElement rootView;
    public HashMap<String, Layer> charCfg = new HashMap<String, Layer>();
    public LinkedList<LayerImage> charLay = new LinkedList<LayerImage>();

    public CharacterGeneratorController() {
        final UITabPane modes = new UITabPane(FontSizes.tabTextHeight, true, false);
        final UIScrollLayout availableOpts = new UIScrollLayout(true, FontSizes.cellSelectScrollersize);
        DBLoader.readFile("CharGen/Modes.txt", new IDatabase() {
            private UICharGenView view;

            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == ':') {
                    modes.addTab(new TabUtils.Tab(view = new UICharGenView(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), CharacterGeneratorController.this), new TabUtils.TabIcon[0]));
                } else if (c == '.') {
                    if (args.length == 1)
                        view.text = args[0];
                    if (args.length == 2)
                        if (TXDB.getLanguage().equals(args[0]))
                            view.text = args[1];
                }
            }
        });
        DBLoader.readFile("CharGen/Layers.txt", new IDatabase() {
            private Layer target;
            private String mode = "Default";
            private final HashMap<String, LinkedList<Layer>> groupsToLayers = new HashMap<String, LinkedList<Layer>>();

            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if ((c == 'x') || (c == ':') || (c == '+')) {
                    final Layer l = target = new Layer(args[0], (c == '+') || (c == 'x'));

                    final LinkedList<String> layerGroups = new LinkedList<String>();

                    l.naming = new UITextButton(args[0], FontSizes.rmaCellTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            if (!l.enabled) {
                                // Being enabled...
                                for (String s : layerGroups) {
                                    for (Layer s2 : groupsToLayers.get(s)) {
                                        s2.enabled = false;
                                        s2.naming.state = false;
                                    }
                                }
                            }
                            l.enabled = !l.enabled;
                            l.naming.state = l.enabled;
                        }
                    }).togglable(l.enabled);
                    l.swatch = new UIColourSwatchButton((int) Long.parseLong(args[1], 16), FontSizes.schemaFieldTextHeight, null);
                    l.swatch.onClick = new Runnable() {
                        @Override
                        public void run() {
                            AppMain.window.createMenu(l.swatch, new UIColourPicker(l.naming.text, l.swatch.col, new IConsumer<Integer>() {
                                @Override
                                public void accept(Integer integer) {
                                    l.swatch.col = integer;
                                }
                            }, true));
                        }
                    };
                    if (c != 'x') {
                        availableOpts.panelsAdd(new UISplitterLayout(l.naming, l.swatch, false, 1));
                    } else {
                        availableOpts.panelsAdd(l.swatch);
                    }
                    charCfg.put(l.id, l);
                    for (int i = 2; i < args.length; i++) {
                        if (!groupsToLayers.containsKey(args[i]))
                            groupsToLayers.put(args[i], new LinkedList<Layer>());
                        groupsToLayers.get(args[i]).add(l);
                        layerGroups.add(args[i]);
                    }
                } else if (c == 'm') {
                    mode = args[0];
                } else if (c == 'i') {
                    charLay.add(new LayerImage(Integer.parseInt(args[1]), "CharGen/" + target.id + args[0] + ".png", mode, target.id));
                } else if (c == '.') {
                    if (args.length == 1)
                        target.naming.text = args[0];
                    if (args.length == 2)
                        if (TXDB.getLanguage().equals(args[0]))
                            target.naming.text = args[1];
                }
            }
        });
        rootView = new UISplitterLayout(modes, availableOpts, false, 1) {
            @Override
            public String toString() {
                return TXDB.get("Character Generator");
            }
        };
        Size sz = AppMain.window.getRootSize();
        rootView.setForcedBounds(null, new Rect(0, 0, sz.width / 2, sz.height / 2));
        Collections.sort(charLay, new Comparator<LayerImage>() {
            @Override
            public int compare(LayerImage layerImage, LayerImage t1) {
                if (layerImage.z < t1.z)
                    return -1;
                if (layerImage.z > t1.z)
                    return 1;
                return 0;
            }
        });
    }

    // An image in the system.
    public class LayerImage {
        public final int z;
        public final String img;
        public final String mode, layerId;

        public LayerImage(int z, String img, String m, String e) {
            this.z = z;
            this.img = img;
            mode = m;
            layerId = e;
        }
    }

    public class Layer {
        public final String id;
        public UITextButton naming;
        public boolean enabled;
        public UIColourSwatchButton swatch;

        public Layer(String i, boolean enable) {
            id = i;
            enabled = enable;
        }
    }
}
