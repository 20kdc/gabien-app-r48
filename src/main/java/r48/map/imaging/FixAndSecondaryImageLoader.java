/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.imaging;

import gabien.IImage;
import gabienapp.Application;
import r48.AppMain;

/**
 * The second-to-outermost layer.
 * Written on October 26th 2017.
 */
public class FixAndSecondaryImageLoader implements IImageLoader {
    public final String prefix, postfix;
    public final IImageLoader loader;

    public FixAndSecondaryImageLoader(String pre, String post, IImageLoader underlying) {
        prefix = pre;
        postfix = post;
        loader = underlying;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        IImage base = loader.getImage(AppMain.rootPath + prefix + name + postfix, panorama);
        if (base == null)
            if (Application.secondaryImageLoadLocation.length() > 0)
                base = loader.getImage(Application.secondaryImageLoadLocation + prefix + name + postfix, panorama);
        return base;
    }

    @Override
    public void flushCache() {

    }
}
