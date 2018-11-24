/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import gabien.GaBIEn;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.ika.IkaMap;
import r48.io.ika.NPChar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 1/27/17.
 */
public class IkaObjectBackend extends OldObjectBackend<IkaMap> {

    private String root;

    public IkaObjectBackend(String rootPath) {
        root = rootPath;
    }

    @Override
    public IkaMap newObject() {
        return new IkaMap(160, 120);
    }

    @Override
    public IkaMap loadObjectFromFile(String filename) {
        if (filename.equals("Map")) {
            byte[] eDataBytes = BMPConnection.prepareBMP(160, 120, 8, 256, false, false);
            byte[] dataBytes = eDataBytes;
            try {
                InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(root + "Pbm/Map1.pbm"));
                if (inp != null) {
                    dataBytes = new byte[inp.available()];
                    if (inp.read(dataBytes) != dataBytes.length) {
                        inp.close();
                        throw new IOException("Available lied");
                    }
                    inp.close();
                } else {
                    // This should be covered by the schema defaults.
                    return null;
                }
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }

            BMPConnection bm;
            try {
                bm = new BMPConnection(eDataBytes, BMPConnection.CMode.Normal, 0, false);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            try {
                bm = new BMPConnection(dataBytes, BMPConnection.CMode.Normal, 0, false);
                if (bm.ignoresPalette)
                    throw new IOException("Must have a palette to do this");
                if (bm.bpp > 8)
                    throw new IOException("Can't be above 8bpp");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            // This sets up the object by itself (DataModel2)

            IkaMap rio = new IkaMap(bm.width, bm.height);

            RubyTable pal = new RubyTable(rio.palette.userVal);
            for (int i = 0; i < bm.paletteCol; i++) {
                int rgba = bm.getPalette(i);
                pal.setTiletype(i, 0, 0, (short) ((rgba >> 24) & 0xFF));
                pal.setTiletype(i, 0, 1, (short) ((rgba >> 16) & 0xFF));
                pal.setTiletype(i, 0, 2, (short) ((rgba >> 8) & 0xFF));
                pal.setTiletype(i, 0, 3, (short) (rgba & 0xFF));
            }

            RubyTable tbl = new RubyTable(rio.data.userVal);

            for (int i = 0; i < bm.width; i++)
                for (int j = 0; j < bm.height; j++)
                    tbl.setTiletype(i, j, 0, (short) bm.getPixel(i, j));

            RubyIO evTbl = rio.events;

            NPChar np = new NPChar();
            try {
                InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(root + "NPChar.dat"));
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
    public void saveObjectToFile(String filename, IkaMap object) throws IOException {
        if (filename.equals("Map")) {
            // allow saving
            RubyTable rt = new RubyTable(object.data.userVal);
            byte[] dataBytes = BMPConnection.prepareBMP(rt.width, rt.height, 8, 256, false, false);
            BMPConnection bm8 = new BMPConnection(dataBytes, BMPConnection.CMode.Normal, 0, false);
            for (int i = 0; i < rt.width; i++)
                for (int j = 0; j < rt.height; j++)
                    bm8.putPixel(i, j, rt.getTiletype(i, j, 0) & 0xFFFF);
            RubyTable rt2 = new RubyTable(object.palette.userVal);
            for (int i = 0; i < 256; i++) {
                int a = rt2.getTiletype(i, 0, 0) & 0xFF;
                int r = rt2.getTiletype(i, 0, 1) & 0xFF;
                int g = rt2.getTiletype(i, 0, 2) & 0xFF;
                int b = rt2.getTiletype(i, 0, 3) & 0xFF;
                bm8.putPalette(i, (a << 24) | (r << 16) | (g << 8) | b);
            }
            OutputStream fio = GaBIEn.getOutFile(PathUtils.autoDetectWindows(root + "Pbm/Map1.pbm"));
            if (fio == null)
                throw new IOException("Unable to open Map1 for writing.");
            fio.write(dataBytes);
            fio.close();

            NPChar npc = new NPChar();
            RubyIO r = object.events;
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

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }
}
