/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.IFunction;
import r48.map.events.RMEventGraphicRenderer;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created on 04/06/17.
 */
public class TSDB {
    public LinkedList<TSPicture> pictures = new LinkedList<TSPicture>();
    public int[] mapping;
    public int xorDoubleclick = 0;
    public boolean disableHex = false;
    public int mulW = 1, mulH = 1;

    public TSDB(String arg) {
        DBLoader.readFile(arg, new IDatabase() {

            public IFunction<Integer, Boolean> acceptable = new IFunction<Integer, Boolean>() {
                @Override
                public Boolean apply(Integer integer) {
                    return true;
                }
            };
            public String image = "layertab.png";

            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'I')
                    image = args[0];
                if (c == 'p')
                    pictures.add(new TSPicture(acceptable, compileData(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), image));
                if (c == 'x')
                    xorDoubleclick = Integer.parseInt(args[0]);
                if (c == 'l') {
                    if (args.length < 2) {
                        acceptable = new IFunction<Integer, Boolean>() {
                            @Override
                            public Boolean apply(Integer integer) {
                                return true;
                            }
                        };
                    } else {
                        final int first = Integer.parseInt(args[0]);
                        final int len = Integer.parseInt(args[1]);
                        acceptable = new IFunction<Integer, Boolean>() {
                            @Override
                            public Boolean apply(Integer integer) {
                                return (integer >= first) && (integer < (first + len));
                            }
                        };
                    }
                }
                if (c == 'X') {
                    mulW = Integer.parseInt(args[0]);
                    mulH = Integer.parseInt(args[1]);
                }
                if (c == 'z')
                    disableHex = true;
                if (c == 't')
                    mapping = new int[Integer.parseInt(args[0])];
                if (c == 'i')
                    mapping[Integer.parseInt(args[0])] = Integer.parseInt(args[1]);
                if (c == '>')
                    DBLoader.readFile(args[0], this);
                if (c == 'r') {
                    int ofs1 = Integer.parseInt(args[0]);
                    int ofs2 = Integer.parseInt(args[1]);
                    int len = Integer.parseInt(args[2]);
                    for (int i = 0; i < len; i++)
                        mapping[ofs1 + i] = ofs2 + i;
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

    public void draw(int x, int y, int t, short tiletype, int sprScale, IGrDriver igd) {
        for (TSDB.TSPicture tsp : pictures) {
            if (!tsp.acceptable.apply(t))
                continue;
            boolean flagValid = tsp.testFlag(tiletype);
            int rtX = flagValid ? tsp.layertabAX : tsp.layertabIX;
            int rtY = flagValid ? tsp.layertabAY : tsp.layertabIY;
            RMEventGraphicRenderer.flexibleSpriteDraw(rtX, rtY, tsp.w, tsp.h, x + (tsp.x * sprScale), y + (tsp.y * sprScale), tsp.w * sprScale, tsp.h * sprScale, 0, GaBIEn.getImageCKEx(tsp.img, false, true, 255, 0, 255), 0, igd);
        }
    }

    public IImage compileSheet(int count, int tileSize) {
        IGrDriver workingImage = GaBIEn.makeOffscreenBuffer(tileSize * count, tileSize, true);
        for (int i = 0; i < count; i++)
            draw(i * tileSize, 0, 0, (short) i, 1, workingImage);
        IImage img2 = GaBIEn.createImage(workingImage.getPixels(), workingImage.getWidth(), workingImage.getHeight());
        workingImage.shutdown();
        return img2;
    }

    public class TSPicture {
        public int layertabIX, layertabIY, layertabAX, layertabAY, x, y, w, h;
        public int[] flagData;
        public String img;
        public IFunction<Integer, Boolean> acceptable;

        public TSPicture(IFunction<Integer, Boolean> accept, int[] i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, String image) {
            acceptable = accept;
            flagData = i;
            layertabIX = i1;
            layertabIY = i2;
            layertabAX = i3;
            layertabAY = i4;
            x = i5;
            y = i6;
            w = i7;
            h = i8;
            img = image;
        }

        public boolean testFlag(short tiletype) {
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
    }
}
