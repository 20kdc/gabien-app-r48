/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import gabien.IGrDriver;
import gabien.IImage;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.PathSyntax;
import r48.dbs.ValueSyntax;
import r48.io.data.IRIO;

import java.io.IOException;
import java.util.HashMap;

/**
 * Provides a simple syntax for defining event images.
 * Created on May 20th 2018
 */
public class GenericEventGraphicRenderer implements IEventGraphicRenderer {
    public String graphicPath;
    public final IImage source;
    public final HashMap<String, GGraphicsInfo> renders = new HashMap<String, GGraphicsInfo>();

    public GenericEventGraphicRenderer(IImage sourceFile, String ctFile) {
        source = sourceFile;
        DBLoader.readFile(ctFile, new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'G')
                    graphicPath = args[0];
                if (c == ':') {
                    GGraphicsInfo ggi = new GGraphicsInfo(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
                    renders.put(args[0], ggi);
                }
            }
        });
    }

    @Override
    public int determineEventLayer(IRIO event) {
        return 0;
    }

    @Override
    public IRIO extractEventGraphic(IRIO event) {
        return PathSyntax.parse(event, graphicPath);
    }

    @Override
    public void drawEventGraphic(IRIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        GGraphicsInfo ggi = renders.get(ValueSyntax.encode(target));
        if (ggi == null)
            return;
        igd.blitScaledImage(ggi.x, ggi.y, ggi.w, ggi.h, ox + ggi.ox, oy + ggi.oy, ggi.w * sprScale, ggi.h * sprScale, source);
    }

    private class GGraphicsInfo {
        final int ox, oy;
        final int x, y, w, h;

        public GGraphicsInfo(int ox, int oy, int x, int y, int w, int h) {
            this.ox = ox;
            this.oy = oy;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
