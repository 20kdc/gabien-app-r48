/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.render.ITexRegion;
import r48.R48;
import r48.ioplus.DBLoader;
import r48.ioplus.IDatabase;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;

/**
 * Created on 04/06/17.
 */
public class TSDB {
    public LinkedList<TSPicture> pictures = new LinkedList<TSPicture>();
    public int[] mapping;
    public int xorDoubleclick = 0;
    public boolean disableHex = false;
    public int tileW = 0, tileH = 0;
    public boolean tileWHSet;

    public TSDB(R48 app, String arg) {
        Consumer<String> loadProgress = app.loadProgress;
        DBLoader.readFile(loadProgress, arg, new IDatabase() {

            public Function<Integer, Boolean> acceptable = (integer) -> {
                return true;
            };

            public IImage image = app.a.layerTabs;

            @Override
            public void newObj(int objId, String objName, DatumSrcLoc sl) throws IOException {

            }

            @Override
            public void execCmd(String c, String[] args, Object[] argsObj, DatumSrcLoc sl) throws IOException {
                if (c.equals("#")) {
                    image = GaBIEn.getImage(args[0]);
                } else if (c.equals("p")) {
                    int w = Integer.parseInt(args[7]);
                    int h = Integer.parseInt(args[8]);
                    ITexRegion imgOff = image.subRegion(Integer.parseInt(args[1]), Integer.parseInt(args[2]), w, h);
                    ITexRegion imgOn = image.subRegion(Integer.parseInt(args[3]), Integer.parseInt(args[4]), w, h);
                    pictures.add(new TSPicture(acceptable, compileData(args[0]), imgOff, imgOn, Integer.parseInt(args[5]), Integer.parseInt(args[6]), w, h));
                } else if (c.equals("P")) {
                    int tileSize = image.height;
                    int tidOff = Integer.parseInt(args[1]);
                    int tidOn = Integer.parseInt(args[2]);
                    ITexRegion imgOff = tidOff != -1 ? image.subRegion(tidOff * tileSize, 0, tileSize, tileSize) : null;
                    ITexRegion imgOn = tidOn != -1 ? image.subRegion(tidOn * tileSize, 0, tileSize, tileSize) : null;
                    pictures.add(new TSPicture(acceptable, compileData(args[0]), imgOff, imgOn, 0, 0, tileSize, tileSize));
                } else if (c.equals("x")) {
                    xorDoubleclick = Integer.parseInt(args[0]);
                } else if (c.equals("l")) {
                    if (args.length < 2) {
                        acceptable = (integer) -> {
                            return true;
                        };
                    } else {
                        final int first = Integer.parseInt(args[0]);
                        final int len = Integer.parseInt(args[1]);
                        acceptable = (integer) -> {
                            return (integer >= first) && (integer < (first + len));
                        };
                    }
                } else if (c.equals("X")) {
                    tileW = Integer.parseInt(args[0]);
                    tileH = Integer.parseInt(args[1]);
                    tileWHSet = true;
                } else if (c.equals("z")) {
                    disableHex = true;
                } else if (c.equals("t")) {
                    mapping = new int[Integer.parseInt(args[0])];
                } else if (c.equals("i")) {
                    mapping[Integer.parseInt(args[0])] = Integer.parseInt(args[1]);
                } else if (c.equals(">")) {
                    DBLoader.readFile(loadProgress, args[0], this);
                } else if (c.equals("r")) {
                    int ofs1 = Integer.parseInt(args[0]);
                    int ofs2 = Integer.parseInt(args[1]);
                    int len = Integer.parseInt(args[2]);
                    for (int i = 0; i < len; i++)
                        mapping[ofs1 + i] = ofs2 + i;
                } else {
                    throw new RuntimeException("Unrecognized: " + c);
                }
            }
        });
    }

    // AND,TARGET,MD
    // MD 0 is (t & AND) != TARGET
    // MD 1 is t within inclusive range AND,TARGET
    private int[] compileData(String arg) {
        String[] rules = arg.split("/");
        int[] rout = new int[3 * rules.length];
        for (int i = 0; i < rules.length; i++) {
            String[] subs = rules[i].split(",");
            for (int j = 0; j < subs.length; j++) {
                if (subs[j].startsWith("0x")) {
                    rout[(i * 3) + j] = Integer.parseInt(subs[j].substring(2), 16);
                } else {
                    rout[(i * 3) + j] = Integer.parseInt(subs[j]);
                }
            }
        }
        return rout;
    }

    public void draw(int x, int y, int t, int tiletype, int sprScale, IGrDriver igd) {
        for (TSDB.TSPicture tsp : pictures)
            tsp.draw(x, y, t, tiletype, sprScale, igd);
    }

    public IImage compileSheet(int count, int tileSize) {
        IGrDriver workingImage = GaBIEn.makeOffscreenBuffer(tileSize * count, tileSize);
        for (int i = 0; i < count; i++)
            draw(i * tileSize, 0, 0, (short) i, 1, workingImage);
        IImage img2 = GaBIEn.createImage(workingImage.getPixels(), workingImage.getWidth(), workingImage.getHeight());
        workingImage.shutdown();
        return img2;
    }

    public class TSPicture {
        public @Nullable ITexRegion imgOff, imgOn;
        public int x, y, w, h;
        public int[] flagData;
        public Function<Integer, Boolean> acceptable;

        public TSPicture(Function<Integer, Boolean> accept, int[] i, ITexRegion imgOff, ITexRegion imgOn, int i5, int i6, int i7, int i8) {
            acceptable = accept;
            flagData = i;
            x = i5;
            y = i6;
            w = i7;
            h = i8;
            this.imgOff = imgOff;
            this.imgOn = imgOn;
        }

        public boolean testFlag(int tiletype) {
            for (int i = 0; i < flagData.length; i += 3) {
                switch (flagData[i + 2]) {
                    case 0:
                        if ((tiletype & flagData[i]) == flagData[i + 1])
                            return false;
                        break;
                    case 1:
                        if ((tiletype < flagData[i]) || (tiletype > flagData[i + 1]))
                            return false;
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        public void draw(int ox, int oy, int t, int tiletype, int sprScale, IGrDriver igd) {
            if (!acceptable.apply(t))
                return;
            boolean flagValid = testFlag(tiletype);
            ITexRegion img = flagValid ? imgOn : imgOff;
            if (img != null)
                igd.blitScaledImage(0, 0, w, h, ox + (x * sprScale), oy + (y * sprScale), w * sprScale, h * sprScale, img);
        }
    }
}
