/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.vfs.FSBackend;
import r48.RubyTable;
import r48.RubyTableR;
import r48.io.data.DMContext;
import r48.io.data.IRIOFixedHash;
import r48.io.ika.IkaEvent;
import r48.io.ika.IkaMap;
import r48.io.ika.NPChar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on 1/27/17.
 */
public class IkaObjectBackend extends OldObjectBackend<IkaMap, IkaMap> {

    private String root;
    public final FSBackend fs;

    public IkaObjectBackend(FSBackend fs, String rootPath) {
        this.fs = fs;
        root = rootPath;
    }

    @Override
    public IkaMap newObjectO(String n, @NonNull DMContext context) {
        return new IkaMap(context, 160, 120);
    }

    @Override
    public IkaMap loadObjectFromFile(String filename, @NonNull DMContext context) {
        if (filename.equals("Map")) {
            byte[] eDataBytes = BMPConnection.prepareBMP(160, 120, 8, 256, false, false);
            byte[] dataBytes = eDataBytes;
            try (InputStream inp = fs.intoPath(root + "Pbm/Map1.pbm").openRead()) {
                dataBytes = new byte[inp.available()];
                if (inp.read(dataBytes) != dataBytes.length)
                    throw new IOException("Available lied");
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }

            BMPConnection bm;
            try {
                bm = new BMPConnection(new ByteArrayMemoryish(eDataBytes), BMPConnection.CMode.Normal, 0, false);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            try {
                bm = new BMPConnection(new ByteArrayMemoryish(dataBytes), BMPConnection.CMode.Normal, 0, false);
                if (bm.ignoresPalette)
                    throw new IOException("Must have a palette to do this");
                if (bm.bpp > 8)
                    throw new IOException("Can't be above 8bpp");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            // This sets up the object by itself (DataModel2)

            IkaMap rio = new IkaMap(context, bm.width, bm.height);

            RubyTable pal = new RubyTable(rio.palette.editUser());
            for (int i = 0; i < bm.paletteCol; i++) {
                int rgba = bm.getPalette(i);
                pal.setTiletype(i, 0, 0, (short) ((rgba >> 24) & 0xFF));
                pal.setTiletype(i, 0, 1, (short) ((rgba >> 16) & 0xFF));
                pal.setTiletype(i, 0, 2, (short) ((rgba >> 8) & 0xFF));
                pal.setTiletype(i, 0, 3, (short) (rgba & 0xFF));
            }

            RubyTable tbl = new RubyTable(rio.data.editUser());

            for (int i = 0; i < bm.width; i++)
                for (int j = 0; j < bm.height; j++)
                    tbl.setTiletype(i, j, 0, (short) bm.getPixel(i, j));

            IRIOFixedHash<Integer, IkaEvent> evTbl = rio.events;

            NPChar np = new NPChar();
            try (InputStream inp = fs.intoPath(root + "NPChar.dat").openRead()) {
                np.load(inp);
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }
            for (int i = 0; i < np.npcTable.length; i++)
                if (np.npcTable[i].exists)
                    evTbl.hashVal.put(i, convertEventToRuby(np.npcTable[i], context));

            return rio;
        }
        return null;
    }

    private IkaEvent convertEventToRuby(NPChar.NPCCharacter io, DMContext dm2c) {
        IkaEvent res = new IkaEvent(dm2c);
        int px = rounder(io.posX);
        int py = rounder(io.posY);
        res.x.setFX(px);
        res.y.setFX(py);
        res.tox.setFX(rounder(io.ofsX) - px);
        res.toy.setFX(rounder(io.ofsY) - py);
        res.type.setFX(io.entityType);
        res.status.setFX(io.entityStatus);
        res.scriptId.setFX(io.eventID);
        res.collisionType.setFX(io.collisionType);
        return res;
    }

    private int rounder(double pos) {
        return (int) (pos + 0.5);
    }

    @Override
    public void saveObjectToFile(String filename, IkaMap object) throws IOException {
        if (filename.equals("Map")) {
            // allow saving
            RubyTableR rt = new RubyTableR(object.data.getBuffer());
            byte[] dataBytes = BMPConnection.prepareBMP(rt.width, rt.height, 8, 256, false, false);
            BMPConnection bm8 = new BMPConnection(new ByteArrayMemoryish(dataBytes), BMPConnection.CMode.Normal, 0, false);
            for (int i = 0; i < rt.width; i++)
                for (int j = 0; j < rt.height; j++)
                    bm8.putPixel(i, j, rt.getTiletype(i, j, 0) & 0xFFFF);
            RubyTableR rt2 = new RubyTableR(object.palette.getBuffer());
            for (int i = 0; i < 256; i++) {
                int a = rt2.getTiletype(i, 0, 0) & 0xFF;
                int r = rt2.getTiletype(i, 0, 1) & 0xFF;
                int g = rt2.getTiletype(i, 0, 2) & 0xFF;
                int b = rt2.getTiletype(i, 0, 3) & 0xFF;
                bm8.putPalette(i, (a << 24) | (r << 16) | (g << 8) | b);
            }
            try (OutputStream fio = fs.intoPath(root + "Pbm/Map1.pbm").openWrite()) {
                fio.write(dataBytes);
            }

            NPChar npc = new NPChar();
            for (int i = 0; i < npc.npcTable.length; i++) {
                IkaEvent r2 = object.events.hashVal.get(i);
                if (r2 != null) {
                    NPChar.NPCCharacter n = npc.npcTable[i];
                    n.exists = true;
                    n.posX = r2.x.getFX();
                    n.posY = r2.y.getFX();
                    n.ofsX = n.posX + r2.tox.getFX();
                    n.ofsY = n.posY + r2.toy.getFX();
                    n.collisionType = (int) r2.collisionType.getFX();
                    n.entityStatus = (int) r2.status.getFX();
                    n.entityType = (int) r2.type.getFX();
                    n.eventID = (int) r2.scriptId.getFX();
                }
            }
            try (OutputStream fio2 = fs.intoPath(root + "NPChar.dat").openWrite()) {
                npc.save(fio2);
            }
            return;
        }
        // do nothing, usually
        throw new IOException("Can't save " + filename);
    }
}
