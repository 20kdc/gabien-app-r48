/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.io;

import gabien.GaBIEn;
import gabienapp.Application;
import r48.RubyCT;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.ika.BM8I;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
        if (filename.equals("MapInfos")) {
            RubyIO map = new RubyIO();
            map.iVars.put("@name", new RubyIO().encString("Map1.pbm"));
            map.iVars.put("@order", new RubyIO().setFX(1));
            map.iVars.put("@parent_id", new RubyIO().setFX(0));

            RubyIO rio = new RubyIO();
            rio.type = '{';
            rio.hashVal = new HashMap<RubyIO, RubyIO>();
            rio.hashVal.put(new RubyIO().setFX(1), map);
            return rio;
        }
        if (filename.equals("Map001")) {
            RubyIO rio = new RubyIO();
            rio.type = 'o';
            rio.symVal = "IkaMap";

            BM8I bm = new BM8I();
            bm.width = 160;
            bm.height = 120;
            bm.data = new int[160 * 120];
            try {
                InputStream inp = GaBIEn.getFile(root + "Pbm/Map1.pbm");
                bm.loadBitmap(inp);
                inp.close();
            } catch (IOException ioe) {
                // Oh well
                ioe.printStackTrace();
            }

            RubyIO mapTbl = new RubyIO();
            mapTbl.type = 'u';
            mapTbl.symVal = "Table";
            RubyTable tbl = new RubyTable(bm.width, bm.height, 1);
            mapTbl.userVal = tbl.innerBytes;

            for (int i = 0; i < bm.width; i++)
                for (int j = 0; j < bm.height; j++)
                    tbl.setTiletype(i, j, 0, (short) bm.data[i + (j * bm.width)]);

            rio.iVars.put("@data", mapTbl);

            RubyIO evTbl = new RubyIO();
            evTbl.type = '{';
            evTbl.hashVal = new HashMap<RubyIO, RubyIO>();
            rio.iVars.put("@events", evTbl);

            return rio;
        }
        return null;
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) {

    }
}
