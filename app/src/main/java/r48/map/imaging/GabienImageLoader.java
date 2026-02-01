/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.imaging;

import java.io.InputStream;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.render.WSIImage;
import r48.App;
import r48.texture.ITexLoader;

/**
 * Does whatever the default can.
 * Basically, if it happens to not support something in a given case, *then* bring explicit support into Gabien,
 * unless it's an obscure format (XYZ), then in which case do something else.
 * Created on 29/05/17.
 */
public class GabienImageLoader extends App.Svc implements ITexLoader {
    public final String postfix;
    public final boolean ck;
    public final int r, g, b;

    public GabienImageLoader(App app, String pp, int cr, int cg, int cb) {
        super(app);
        postfix = pp;
        ck = true;
        r = cr;
        g = cg;
        b = cb;
    }

    public GabienImageLoader(App app, String pp) {
        super(app);
        postfix = pp;
        ck = false;
        r = 0;
        g = 0;
        b = 0;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        try (InputStream inp = app.gameResources.intoPath(name + postfix).openRead()) {
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
