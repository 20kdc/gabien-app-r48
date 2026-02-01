/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.imaging;

import gabien.render.IImage;
import r48.App;
import r48.texture.ITexLoader;

/**
 * The second-to-outermost layer.
 * Written on October 26th 2017.
 */
public class FixAndSecondaryImageLoader extends App.Svc implements ITexLoader {
    public final String prefix, postfix;
    public final ITexLoader loader;

    public FixAndSecondaryImageLoader(App app, String pre, String post, ITexLoader underlying) {
        super(app);
        prefix = pre;
        postfix = post;
        loader = underlying;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        return loader.getImage(prefix + name + postfix, panorama);
    }

    @Override
    public void flushCache() {
        loader.flushCache();
    }
}
