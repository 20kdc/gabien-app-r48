/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import gabien.GaBIEn;
import gabienapp.Application;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.ika.BM8I;
import r48.io.ika.NPChar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 1/27/17.
 */
public class IkaObjectBackend implements IObjectBackend {

    private String root;

    public IkaObjectBackend(String rootPath) {
        root = rootPath;
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        if (filename.equals("Map")) {
            RubyIO rio = new RubyIO().setSymlike("IkachanMap", true);

            BM8I bm = new BM8I();
            bm.width = 160;
            bm.height = 120;
            bm.data = new int[160 * 120];
            bm.palette = new int[256];
            try {
                InputStream inp = GaBIEn.getFile(PathUtils.autoDetectWindows(root + "Pbm/Map1.pbm"));
                if (inp != null) {
                    bm.loadBitmap(inp);
                    inp.close();
                } else {
                    // This should be covered by the schema defaults.
                    return null;
                }
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }

            RubyTable pal = new RubyTable(256, 1, 4, new int[4]);
            for (int i = 0; i < 256; i++) {
                int rgba = bm.palette[i];
                pal.setTiletype(i, 0, 0, (short) ((rgba >> 24) & 0xFF));
                pal.setTiletype(i, 0, 1, (short) ((rgba >> 16) & 0xFF));
                pal.setTiletype(i, 0, 2, (short) ((rgba >> 8) & 0xFF));
                pal.setTiletype(i, 0, 3, (short) (rgba & 0xFF));
            }
            RubyIO palTbl = new RubyIO().setUser("Table", pal.innerBytes);

            RubyTable tbl = new RubyTable(bm.width, bm.height, 1, new int[1]);
            RubyIO mapTbl = new RubyIO().setUser("Table", tbl.innerBytes);

            for (int i = 0; i < bm.width; i++)
                for (int j = 0; j < bm.height; j++)
                    tbl.setTiletype(i, j, 0, (short) bm.data[i + (j * bm.width)]);

            rio.addIVar("@data", mapTbl);
            rio.addIVar("@palette", palTbl);

            RubyIO evTbl = new RubyIO().setHash();
            rio.addIVar("@events", evTbl);

            NPChar np = new NPChar();
            try {
                InputStream inp = GaBIEn.getFile(PathUtils.autoDetectWindows(root + "NPChar.dat"));
                np.load(inp);
                inp.close();
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }
            for (int i = 0; i < np.npcTable.length; i++)
                if (np.npcTable[i].exists)
                    evTbl.hashVal.put(new RubyIO().setFX(i), convertEventToRuby(np.npcTable[i]));

            return rio;
        }
        return null;
    }

    private RubyIO convertEventToRuby(NPChar.NPCCharacter io) {
        RubyIO res = new RubyIO().setSymlike("IkachanEvent", true);
        int px = rounder(io.posX);
        int py = rounder(io.posY);
        res.addIVar("@x", new RubyIO().setFX(px));
        res.addIVar("@y", new RubyIO().setFX(py));
        res.addIVar("@tOX", new RubyIO().setFX(rounder(io.ofsX) - px));
        res.addIVar("@tOY", new RubyIO().setFX(rounder(io.ofsY) - py));
        res.addIVar("@type", new RubyIO().setFX(io.entityType));
        res.addIVar("@status", new RubyIO().setFX(io.entityStatus));
        res.addIVar("@scriptId", new RubyIO().setFX(io.eventID));
        res.addIVar("@collisionType", new RubyIO().setFX(io.collisionType));
        return res;
    }

    private int rounder(double pos) {
        return (int) (pos + 0.5);
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
        if (filename.equals("Map")) {
            // allow saving
            BM8I bm8 = new BM8I();
            RubyTable rt = new RubyTable(object.getInstVarBySymbol("@data").userVal);
            bm8.width = rt.width;
            bm8.height = rt.height;
            bm8.data = new int[bm8.width * bm8.height];
            bm8.palette = new int[256];
            for (int i = 0; i < rt.width; i++)
                for (int j = 0; j < rt.height; j++)
                    bm8.data[i + (j * rt.width)] = (int) rt.getTiletype(i, j, 0);
            RubyTable rt2 = new RubyTable(object.getInstVarBySymbol("@palette").userVal);
            for (int i = 0; i < 256; i++) {
                int a = rt2.getTiletype(i, 0, 0) & 0xFF;
                int r = rt2.getTiletype(i, 0, 1) & 0xFF;
                int g = rt2.getTiletype(i, 0, 2) & 0xFF;
                int b = rt2.getTiletype(i, 0, 3) & 0xFF;
                bm8.palette[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
            OutputStream fio = GaBIEn.getOutFile(PathUtils.autoDetectWindows(root + "Pbm/Map1.pbm"));
            if (fio == null)
                throw new IOException("Unable to open Map1 for writing.");
            bm8.saveBitmap(fio);
            fio.close();

            NPChar npc = new NPChar();
            RubyIO r = object.getInstVarBySymbol("@events");
            for (int i = 0; i < npc.npcTable.length; i++) {
                RubyIO r2 = r.getHashVal(new RubyIO().setFX(i));
                if (r2 != null) {
                    NPChar.NPCCharacter n = npc.npcTable[i];
                    n.exists = true;
                    n.posX = r2.getInstVarBySymbol("@x").fixnumVal;
                    n.posY = r2.getInstVarBySymbol("@y").fixnumVal;
                    n.ofsX = n.posX + r2.getInstVarBySymbol("@tOX").fixnumVal;
                    n.ofsY = n.posY + r2.getInstVarBySymbol("@tOY").fixnumVal;
                    n.collisionType = (int) r2.getInstVarBySymbol("@collisionType").fixnumVal;
                    n.entityStatus = (int) r2.getInstVarBySymbol("@status").fixnumVal;
                    n.entityType = (int) r2.getInstVarBySymbol("@type").fixnumVal;
                    n.eventID = (int) r2.getInstVarBySymbol("@scriptId").fixnumVal;
                }
            }
            fio = GaBIEn.getOutFile(PathUtils.autoDetectWindows(root + "NPChar.dat"));
            npc.save(fio);
            fio.close();
            return;
        }
        // do nothing, usually
        throw new IOException("Can't save " + filename);
    }
}
