/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dmicg;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.WSIImage;
import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import gabien.uslx.append.*;
import gabien.uslx.io.ByteArrayMemoryish;
import r48.App;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.imageio.PNG8IImageIOFormat;
import r48.io.BMPConnection;
import r48.io.data.IRIOGeneric;
import r48.ui.UIAppendButton;
import r48.ui.UIColourSwatchButton;
import r48.ui.dialog.UIColourPicker;
import r48.ui.utilitybelt.ImageEditorImage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Character Generator
 * Demetrius wants this, so I'm doing this.
 * Created on December 16, 2018.
 */
public class CharacterGeneratorController extends App.Svc {
    public UIElement rootView;
    public HashMap<String, Layer> charCfg = new HashMap<String, Layer>();
    public LinkedList<LayerImage> charLay = new LinkedList<LayerImage>();

    private final UITabPane modes;
    private LinkedList<UICharGenView> views = new LinkedList<UICharGenView>();

    public CharacterGeneratorController(App app) {
        super(app);
        modes = new UITabPane(app.f.tabTH, true, false);
        DBLoader.readFile(app, "CharGen/Modes", new IDatabase() {
            private UICharGenView view;

            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(String c, String[] args) throws IOException {
                if (c.equals(":")) {
                    view = new UICharGenView(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), CharacterGeneratorController.this);
                    modes.addTab(new UITabBar.Tab(view, new UITabBar.TabIcon[0]));
                    views.add(view);
                } else if (c.equals(".")) {
                    if (args.length == 1)
                        view.text = args[0];
                    if (args.length == 2)
                        if (app.c.language.equals(args[0]))
                            view.text = args[1];
                } else {
                    throw new RuntimeException("Unknown command " + c);
                }
            }
        });
        LinkedList<UIElement> availableOpts = new LinkedList<>();
        DBLoader.readFile(app, "CharGen/Layers", new IDatabase() {
            private Layer target;
            private String mode = "Default";
            private final HashMap<String, LinkedList<Layer>> groupsToLayers = new HashMap<String, LinkedList<Layer>>();

            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(String c, String[] args) throws IOException {
                if (c.equals("x") || c.equals(":") || c.equals("+")) {
                    final Layer l = target = new Layer(args[0], c.equals("+") || c.equals("x"));

                    final LinkedList<String> layerGroups = new LinkedList<String>();

                    l.naming = new UITextButton(args[0], app.f.rmaCellTH, () -> {
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
                    }).togglable(l.enabled);
                    l.swatch = new UIColourSwatchButton((int) Long.parseLong(args[1], 16), app.f.schemaFieldTH, null);
                    l.swatch.onClick = () -> {
                        app.ui.wm.createMenu(l.swatch, new UIColourPicker(app, l.naming.getText(), l.swatch.col, (integer) -> {
                            if (integer != null)
                                l.swatch.col = integer;
                        }, true));
                    };
                    if (!c.equals("x")) {
                        availableOpts.add(new UISplitterLayout(l.naming, l.swatch, false, 1));
                    } else {
                        availableOpts.add(l.swatch);
                    }
                    charCfg.put(l.id, l);
                    for (int i = 2; i < args.length; i++) {
                        if (!groupsToLayers.containsKey(args[i]))
                            groupsToLayers.put(args[i], new LinkedList<Layer>());
                        groupsToLayers.get(args[i]).add(l);
                        layerGroups.add(args[i]);
                    }
                } else if (c.equals("m")) {
                    mode = args[0];
                } else if (c.equals("i")) {
                    charLay.add(new LayerImage(Integer.parseInt(args[1]), "CharGen/" + target.id + args[0] + ".png", mode, target.id));
                } else if (c.equals(".")) {
                    if (args.length == 1)
                        target.naming.setText(args[0]);
                    if (args.length == 2)
                        if (app.c.language.equals(args[0]))
                            target.naming.setText(args[1]);
                } else {
                    throw new RuntimeException("Unknown command: " + c);
                }
            }
        });
        UIElement modeBar = new UIAppendButton(T.u.cg_savePNG, new UITextButton(T.u.cg_copyR48, app.f.schemaFieldTH, () -> {
            WSIImage img = getCurrentModeImage();
            int[] tx = img.getPixels();
            int w = img.width;
            int h = img.height;
            int idx = 0;
            byte[] buffer = BMPConnection.prepareBMP(w, h, 32, 0, true, false);
            BMPConnection bc;
            try {
                bc = new BMPConnection(new ByteArrayMemoryish(buffer), BMPConnection.CMode.Normal, 0, false);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            for (int j = 0; j < h; j++)
                for (int i = 0; i < w; i++)
                    bc.putPixel(i, j, tx[idx++]);
            app.theClipboard = new IRIOGeneric(app.ctxClipboardUTF8Encoding).setUser("Image", buffer);
        }), () -> {
            // We have a PNG, ask for a file to stuff it into
            final byte[] b = createPNG();
            GaBIEn.startFileBrowser(T.u.cg_savePNG, true, "", new Consumer<String>() {
                @Override
                public void accept(String s) {
                    try {
                        OutputStream os = GaBIEn.getOutFile(s);
                        os.write(b);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        app.ui.launchDialog(e);
                    }
                }
            });
        }, app.f.schemaFieldTH);
        final UIScrollLayout availableOptsSVL = new UIScrollLayout(true, app.f.cellSelectS, availableOpts);
        rootView = new UISplitterLayout(new UISplitterLayout(modes, modeBar, true, 1), availableOptsSVL, false, 1) {
            @Override
            public String toString() {
                return T.t.charGen;
            }
        };
        Size sz = app.ui.wm.getRootSize();
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

    private byte[] createPNG() {
        // This is not going to go well...
        WSIImage img = getCurrentModeImage();
        ImageEditorImage iei = new ImageEditorImage(img.width, img.height, img.getPixels(), null, false);
        ImageEditorImage iei2 = new ImageEditorImage(iei, true, true);

        if (iei2.paletteSize() > 256) {
            // Cannot make a valid image this complex. Swap out the (2k/2k3 valid) 8I format for another.
            return img.createPNG();
        }

        PNG8IImageIOFormat tempFmt = new PNG8IImageIOFormat(app.ilg);
        try {
            return tempFmt.saveFile(iei2);
        } catch (IOException e) {
            e.printStackTrace();
            return img.createPNG();
        }
    }

    private WSIImage getCurrentModeImage() {
        int modesIdx = modes.getTabIndex();
        if (modesIdx != -1) {
            UICharGenView cgv = views.get(modes.getTabIndex());
            int w = cgv.genWidth;
            int h = cgv.genHeight;
            IGrDriver ib = GaBIEn.makeOffscreenBuffer(w, h);

            cgv.render(ib, cgv.genWidth, cgv.genHeight);

            WSIImage img = GaBIEn.createWSIImage(ib.getPixels(), w, h);
            ib.shutdown();
            return img;
        }
        return GaBIEn.createWSIImage(new int[1], 1, 1);
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
