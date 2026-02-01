/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.texture;

import java.io.InputStream;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.render.WSIImage;
import gabien.uslx.vfs.FSBackend;

/**
 * Does whatever the default can.
 * Basically, if it happens to not support something in a given case, *then* bring explicit support into Gabien,
 * unless it's an obscure format (XYZ), then in which case do something else.
 * Created on 29/05/17.
 */
public class GabienTexLoader implements ITexLoader {
    public final FSBackend src;
    public final String postfix;
    public final boolean ck;
    public final int r, g, b;

    public GabienTexLoader(FSBackend src, String pp, int cr, int cg, int cb) {
        this.src = src;
        postfix = pp;
        ck = true;
        r = cr;
        g = cg;
        b = cb;
    }

    public GabienTexLoader(FSBackend src, String pp) {
        this.src = src;
        postfix = pp;
        ck = false;
        r = 0;
        g = 0;
        b = 0;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        try (InputStream inp = src.intoPath(name + postfix).openRead()) {
            WSIImage wsi = GaBIEn.decodeWSIImage(inp);
            if (wsi == null)
                return null;
            if (ck) {
                return GaBIEn.wsiToCK("gameCK:" + name + postfix, wsi, r, g, b);
            }
            return wsi.upload("game:" + name + postfix);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void flushCache() {
        GaBIEn.hintFlushAllTheCaches();
    }
}
